package com.vectracore.index;

import com.vectracore.distance.DistanceFunction;
import com.vectracore.distance.DistanceMetric;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

public class HnswGraphIndex {

    private final int dimension;
    private final DistanceMetric metric;
    private final int M;
    private final int M0;
    private final int efConstruction;
    private final int efSearch;
    private final double mL;
    private final boolean useQuantization;

    private final Map<String, VectorNode> nodes = new ConcurrentHashMap<>();
    private volatile VectorNode entryPoint = null;
    private volatile int maxCurrentLevel = -1;

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Random rng = new Random(42);

    public HnswGraphIndex(int dimension, DistanceMetric metric, int M, int efConstruction, int efSearch, boolean useQuantization) {
        this.dimension = dimension;
        this.metric = metric;
        this.M = M > 0 ? M : 16;
        this.M0 = 2 * this.M;
        this.efConstruction = efConstruction > 0 ? efConstruction : 64;
        this.efSearch = efSearch > 0 ? efSearch : 32;
        this.mL = 1.0 / Math.log(this.M);
        this.useQuantization = useQuantization;
    }

    public int getRandomLevel() {
        double r = rng.nextDouble();
        if (r == 0.0) r = 0.0000001;
        return (int) Math.floor(-Math.log(r) * mL);
    }

    public float getDistance(float[] a, float[] b) {
        return DistanceFunction.compute(metric, a, b);
    }

    public float getDistance(VectorNode a, VectorNode b) {
        return getDistance(a.getVector(), b.getVector());
    }

    public void insert(String id, float[] vector, Map<String, Object> metadata) {
        if (vector.length != dimension) {
            throw new IllegalArgumentException("Vector dimension " + vector.length + " does not match collection dimension " + dimension);
        }

        rwLock.writeLock().lock();
        try {
            int nodeLevel = getRandomLevel();
            VectorNode newNode = new VectorNode(id, vector, metadata, nodeLevel, useQuantization);
            nodes.put(id, newNode);

            if (entryPoint == null) {
                entryPoint = newNode;
                maxCurrentLevel = nodeLevel;
                return;
            }

            VectorNode currObj = entryPoint;
            float curDist = getDistance(newNode, currObj);

            // 1. Greedy routing down to level nodeLevel + 1
            for (int l = maxCurrentLevel; l > nodeLevel; l--) {
                boolean changed = true;
                while (changed) {
                    changed = false;
                    for (VectorNode neighbor : currObj.getNeighbors(l)) {
                        float d = getDistance(newNode, neighbor);
                        if (d < curDist) {
                            curDist = d;
                            currObj = neighbor;
                            changed = true;
                        }
                    }
                }
            }

            // 2. From min(maxCurrentLevel, nodeLevel) down to level 0: search and connect
            int topLevel = Math.min(maxCurrentLevel, nodeLevel);
            Set<VectorNode> epSet = new HashSet<>();
            epSet.add(currObj);

            for (int l = topLevel; l >= 0; l--) {
                PriorityQueue<SearchResult> candidates = searchLayer(newNode.getVector(), epSet, efConstruction, l);
                int maxM = (l == 0) ? M0 : M;
                List<VectorNode> neighbors = selectNeighbors(newNode, candidates, maxM);

                for (VectorNode neighbor : neighbors) {
                    newNode.addNeighbor(l, neighbor);
                    neighbor.addNeighbor(l, newNode);

                    // Shrink neighbor connections if over capacity
                    int maxNeighM = (l == 0) ? M0 : M;
                    if (neighbor.getNeighbors(l).size() > maxNeighM) {
                        pruneConnections(neighbor, l, maxNeighM);
                    }
                }

                epSet.clear();
                for (SearchResult sr : candidates) {
                    epSet.add(sr.getNode());
                }
            }

            if (nodeLevel > maxCurrentLevel) {
                maxCurrentLevel = nodeLevel;
                entryPoint = newNode;
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private PriorityQueue<SearchResult> searchLayer(float[] query, Set<VectorNode> enterPoints, int ef, int level) {
        Set<VectorNode> visited = new HashSet<>();
        PriorityQueue<SearchResult> candidates = new PriorityQueue<>(); // Min-heap by distance
        PriorityQueue<SearchResult> results = new PriorityQueue<>((a, b) -> Float.compare(b.getDistance(), a.getDistance())); // Max-heap

        for (VectorNode ep : enterPoints) {
            float dist = getDistance(query, ep.getVector());
            SearchResult sr = new SearchResult(ep, dist);
            visited.add(ep);
            candidates.offer(sr);
            results.offer(sr);
        }

        while (!candidates.isEmpty()) {
            SearchResult current = candidates.poll();
            SearchResult furthestResult = results.peek();

            if (current.getDistance() > furthestResult.getDistance()) {
                break;
            }

            for (VectorNode neighbor : current.getNode().getNeighbors(level)) {
                if (visited.add(neighbor)) {
                    float dist = getDistance(query, neighbor.getVector());
                    furthestResult = results.peek();

                    if (dist < furthestResult.getDistance() || results.size() < ef) {
                        SearchResult candidate = new SearchResult(neighbor, dist);
                        candidates.offer(candidate);
                        results.offer(candidate);

                        if (results.size() > ef) {
                            results.poll(); // Remove farthest
                        }
                    }
                }
            }
        }

        return results;
    }

    private List<VectorNode> selectNeighbors(VectorNode queryNode, PriorityQueue<SearchResult> candidates, int m) {
        List<SearchResult> sorted = new ArrayList<>(candidates);
        Collections.sort(sorted); // Ascending order of distance

        List<VectorNode> selected = new ArrayList<>();
        for (SearchResult sr : sorted) {
            if (sr.getNode().equals(queryNode)) continue;
            selected.add(sr.getNode());
            if (selected.size() >= m) break;
        }
        return selected;
    }

    private void pruneConnections(VectorNode node, int level, int maxM) {
        Set<VectorNode> neighbors = node.getNeighbors(level);
        if (neighbors.size() <= maxM) return;

        List<SearchResult> scored = new ArrayList<>();
        for (VectorNode n : neighbors) {
            scored.add(new SearchResult(n, getDistance(node, n)));
        }
        Collections.sort(scored);

        neighbors.clear();
        for (int i = 0; i < maxM && i < scored.size(); i++) {
            neighbors.add(scored.get(i).getNode());
        }
    }

    public List<SearchResult> search(float[] query, int k, Integer ef, Predicate<VectorNode> filter) {
        if (query.length != dimension) {
            throw new IllegalArgumentException("Query dimension " + query.length + " does not match collection dimension " + dimension);
        }
        if (nodes.isEmpty()) return Collections.emptyList();

        rwLock.readLock().lock();
        try {
            if (entryPoint == null) return Collections.emptyList();

            int efVal = (ef != null && ef > 0) ? Math.max(ef, k) : Math.max(efSearch, k);

            VectorNode currObj = entryPoint;
            float curDist = getDistance(query, currObj.getVector());

            // 1. Greedy search from top level down to 1
            for (int l = maxCurrentLevel; l > 0; l--) {
                boolean changed = true;
                while (changed) {
                    changed = false;
                    for (VectorNode neighbor : currObj.getNeighbors(l)) {
                        float d = getDistance(query, neighbor.getVector());
                        if (d < curDist) {
                            curDist = d;
                            currObj = neighbor;
                            changed = true;
                        }
                    }
                }
            }

            // 2. Search bottom layer 0 with dynamic candidate queue
            Set<VectorNode> epSet = Collections.singleton(currObj);
            PriorityQueue<SearchResult> layerResults = searchLayer(query, epSet, efVal, 0);

            // 3. Extract and filter
            List<SearchResult> list = new ArrayList<>();
            while (!layerResults.isEmpty()) {
                SearchResult sr = layerResults.poll();
                if (filter == null || filter.test(sr.getNode())) {
                    list.add(sr);
                }
            }

            Collections.sort(list); // Ascending by distance
            if (list.size() > k) {
                return list.subList(0, k);
            }
            return list;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Exact linear scan used for testing, recall benchmarking, and validation.
     */
    public List<SearchResult> bruteForceSearch(float[] query, int k, Predicate<VectorNode> filter) {
        rwLock.readLock().lock();
        try {
            List<SearchResult> all = new ArrayList<>();
            for (VectorNode node : nodes.values()) {
                if (filter == null || filter.test(node)) {
                    float dist = getDistance(query, node.getVector());
                    all.add(new SearchResult(node, dist));
                }
            }
            Collections.sort(all);
            return all.subList(0, Math.min(k, all.size()));
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public int size() { return nodes.size(); }
    public int getMaxLevel() { return maxCurrentLevel; }
    public int getDimension() { return dimension; }
    public DistanceMetric getMetric() { return metric; }
    public VectorNode getNode(String id) { return nodes.get(id); }
    public boolean contains(String id) { return nodes.containsKey(id); }
}
