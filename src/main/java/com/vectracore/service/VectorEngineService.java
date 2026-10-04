package com.vectracore.service;

import com.vectracore.collection.CollectionConfig;
import com.vectracore.collection.VectorCollection;
import com.vectracore.distance.DistanceFunction;
import com.vectracore.distance.DistanceMetric;
import com.vectracore.dto.*;
import com.vectracore.exception.CollectionNotFoundException;
import com.vectracore.exception.DimensionMismatchException;
import com.vectracore.index.SearchResult;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class VectorEngineService {
    private static final Logger log = LoggerFactory.getLogger(VectorEngineService.class);

    private final Map<String, VectorCollection> collections = new ConcurrentHashMap<>();

    @Value("${vectra.engine.preload-demo-collection:true}")
    private boolean preloadDemo;

    @Value("${vectra.engine.demo-collection-name:vision-embeddings-128}")
    private String demoCollectionName;

    @Value("${vectra.engine.demo-dimension:128}")
    private int demoDimension;

    @Value("${vectra.engine.demo-size:2000}")
    private int demoSize;

    @Value("${vectra.engine.default-m:16}")
    private int defaultM;

    @Value("${vectra.engine.default-ef-construction:64}")
    private int defaultEfConstruction;

    @Value("${vectra.engine.default-ef-search:32}")
    private int defaultEfSearch;

    @PostConstruct
    public void initialize() {
        if (!preloadDemo) return;

        log.info("Initializing Vectra Core with demo collection: {} (dim={}, size={})",
                demoCollectionName, demoDimension, demoSize);

        CollectionConfig config = new CollectionConfig(
                demoCollectionName,
                demoDimension,
                DistanceMetric.COSINE,
                defaultM,
                defaultEfConstruction,
                defaultEfSearch,
                false
        );
        VectorCollection collection = new VectorCollection(config);

        Random rng = new Random(42);
        String[] categories = {"electronics", "apparel", "furniture", "automotive", "books"};

        long start = System.currentTimeMillis();
        for (int i = 0; i < demoSize; i++) {
            float[] vec = new float[demoDimension];
            for (int d = 0; d < demoDimension; d++) {
                vec[d] = (float) rng.nextGaussian();
            }
            vec = DistanceFunction.normalize(vec);

            Map<String, Object> meta = new HashMap<>();
            meta.put("category", categories[rng.nextInt(categories.length)]);
            meta.put("price", 10 + rng.nextInt(500));
            meta.put("itemNumber", i);

            collection.insert("img_" + i, vec, meta);
        }

        long elapsed = System.currentTimeMillis() - start;
        collections.put(demoCollectionName, collection);

        log.info("Demo collection ready: {} vectors indexed into HNSW in {} ms (maxLevel={})",
                collection.size(), elapsed, collection.getMaxLevel());
    }

    public CollectionStatsResponse createCollection(CreateCollectionRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("Collection name cannot be empty");
        }
        if (collections.containsKey(req.getName())) {
            throw new IllegalArgumentException("Collection " + req.getName() + " already exists");
        }

        int m = (req.getM() != null && req.getM() > 0) ? req.getM() : defaultM;
        int efC = (req.getEfConstruction() != null && req.getEfConstruction() > 0) ? req.getEfConstruction() : defaultEfConstruction;
        int efS = (req.getEfSearch() != null && req.getEfSearch() > 0) ? req.getEfSearch() : defaultEfSearch;
        boolean quant = Boolean.TRUE.equals(req.getUseQuantization());

        CollectionConfig config = new CollectionConfig(
                req.getName(),
                req.getDimension(),
                req.getMetric(),
                m,
                efC,
                efS,
                quant
        );

        VectorCollection collection = new VectorCollection(config);
        collections.put(req.getName(), collection);

        return getCollectionStats(req.getName());
    }

    public boolean deleteCollection(String name) {
        return collections.remove(name) != null;
    }

    public VectorCollection getCollection(String name) {
        VectorCollection c = collections.get(name);
        if (c == null) {
            throw new CollectionNotFoundException("Collection '" + name + "' not found");
        }
        return c;
    }

    public void insert(String collectionName, String id, float[] vector, Map<String, Object> metadata) {
        VectorCollection coll = getCollection(collectionName);
        if (vector.length != coll.getConfig().getDimension()) {
            throw new DimensionMismatchException("Vector dimension " + vector.length + " does not match collection " + coll.getConfig().getDimension());
        }
        coll.insert(id, vector, metadata);
    }

    public int batchInsert(String collectionName, List<InsertVectorRequest> records) {
        VectorCollection coll = getCollection(collectionName);
        int inserted = 0;
        for (InsertVectorRequest r : records) {
            if (r.getVector().length != coll.getConfig().getDimension()) {
                throw new DimensionMismatchException("Vector dimension " + r.getVector().length + " does not match collection " + coll.getConfig().getDimension());
            }
            coll.insert(r.getId(), r.getVector(), r.getMetadata());
            inserted++;
        }
        return inserted;
    }

    public SearchResponse search(String collectionName, float[] vector, int k, Integer ef, Map<String, Object> filter) {
        VectorCollection coll = getCollection(collectionName);
        if (vector.length != coll.getConfig().getDimension()) {
            throw new DimensionMismatchException("Query vector dimension " + vector.length + " does not match collection " + coll.getConfig().getDimension());
        }

        long start = System.nanoTime();
        List<SearchResult> results = coll.search(vector, k, ef, filter);
        double elapsedMs = (System.nanoTime() - start) / 1_000_000.0;

        List<SearchResultItem> items = results.stream()
                .map(r -> new SearchResultItem(
                        r.getNode().getId(),
                        r.getDistance(),
                        r.getSimilarity(),
                        r.getNode().getMetadata()
                ))
                .collect(Collectors.toList());

        return new SearchResponse(collectionName, k, items, elapsedMs);
    }

    public CollectionStatsResponse getCollectionStats(String name) {
        VectorCollection coll = getCollection(name);
        CollectionConfig cfg = coll.getConfig();

        return new CollectionStatsResponse(
                cfg.getName(),
                cfg.getDimension(),
                cfg.getMetric(),
                coll.size(),
                coll.getMaxLevel(),
                coll.getTotalSearchQueries(),
                cfg.isUseQuantization(),
                coll.getCreatedAtEpoch()
        );
    }

    public SystemStatsResponse getSystemStats() {
        int totalColl = collections.size();
        int totalVectors = collections.values().stream().mapToInt(VectorCollection::size).sum();

        Runtime rt = Runtime.getRuntime();
        long memMb = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);

        return new SystemStatsResponse(
                totalColl,
                totalVectors,
                memMb + " MB",
                Runtime.getRuntime().availableProcessors(),
                Thread.currentThread().isVirtual(),
                new ArrayList<>(collections.keySet())
        );
    }
}
