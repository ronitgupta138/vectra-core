package com.vectracore.collection;

import com.vectracore.index.HnswGraphIndex;
import com.vectracore.index.SearchResult;
import com.vectracore.index.VectorNode;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class VectorCollection {
    private final CollectionConfig config;
    private final HnswGraphIndex index;
    private final long createdAtEpoch;
    private final AtomicLong totalSearchQueries = new AtomicLong(0);

    public VectorCollection(CollectionConfig config) {
        this.config = config;
        this.index = new HnswGraphIndex(
                config.getDimension(),
                config.getMetric(),
                config.getM(),
                config.getEfConstruction(),
                config.getEfSearch(),
                config.isUseQuantization()
        );
        this.createdAtEpoch = System.currentTimeMillis();
    }

    public void insert(String id, float[] vector, Map<String, Object> metadata) {
        index.insert(id, vector, metadata);
    }

    public void batchInsert(List<Map.Entry<String, float[]>> items, List<Map<String, Object>> metadatas) {
        for (int i = 0; i < items.size(); i++) {
            Map.Entry<String, float[]> entry = items.get(i);
            Map<String, Object> meta = (metadatas != null && i < metadatas.size()) ? metadatas.get(i) : null;
            insert(entry.getKey(), entry.getValue(), meta);
        }
    }

    public List<SearchResult> search(float[] query, int k, Integer ef, Map<String, Object> filterMap) {
        totalSearchQueries.incrementAndGet();
        Predicate<VectorNode> predicate = buildFilterPredicate(filterMap);
        return index.search(query, k, ef, predicate);
    }

    public List<SearchResult> bruteForceSearch(float[] query, int k, Map<String, Object> filterMap) {
        Predicate<VectorNode> predicate = buildFilterPredicate(filterMap);
        return index.bruteForceSearch(query, k, predicate);
    }

    public VectorNode get(String id) {
        return index.getNode(id);
    }

    public boolean contains(String id) {
        return index.contains(id);
    }

    public int size() {
        return index.size();
    }

    public CollectionConfig getConfig() {
        return config;
    }

    public long getCreatedAtEpoch() {
        return createdAtEpoch;
    }

    public long getTotalSearchQueries() {
        return totalSearchQueries.get();
    }

    public int getMaxLevel() {
        return index.getMaxLevel();
    }

    private Predicate<VectorNode> buildFilterPredicate(Map<String, Object> filterMap) {
        if (filterMap == null || filterMap.isEmpty()) {
            return null;
        }
        return node -> {
            Map<String, Object> meta = node.getMetadata();
            for (Map.Entry<String, Object> entry : filterMap.entrySet()) {
                Object actual = meta.get(entry.getKey());
                if (actual == null || !actual.toString().equalsIgnoreCase(entry.getValue().toString())) {
                    return false;
                }
            }
            return true;
        };
    }
}
