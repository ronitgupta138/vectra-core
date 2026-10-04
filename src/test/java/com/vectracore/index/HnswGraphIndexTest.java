package com.vectracore.index;

import com.vectracore.distance.DistanceFunction;
import com.vectracore.distance.DistanceMetric;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class HnswGraphIndexTest {

    @Test
    void testBasicInsertionAndExactNearestNeighbor() {
        int dim = 16;
        HnswGraphIndex index = new HnswGraphIndex(dim, DistanceMetric.EUCLIDEAN, 16, 64, 32, false);

        float[] v1 = new float[dim];
        Arrays.fill(v1, 1.0f);

        float[] v2 = new float[dim];
        Arrays.fill(v2, 5.0f);

        index.insert("node_1", v1, Map.of("category", "alpha"));
        index.insert("node_2", v2, Map.of("category", "beta"));

        assertEquals(2, index.size());

        // Query identical to v1
        List<SearchResult> results = index.search(v1, 1, 32, null);
        assertFalse(results.isEmpty());
        assertEquals("node_1", results.get(0).getNode().getId());
        assertEquals(0.0f, results.get(0).getDistance(), 1e-4f);
    }

    @Test
    void testHighRecallVsBruteForce() {
        int dim = 32;
        int numNodes = 250;
        int k = 5;

        HnswGraphIndex index = new HnswGraphIndex(dim, DistanceMetric.COSINE, 16, 64, 32, false);
        Random rng = new Random(138);

        List<float[]> vectors = new ArrayList<>();
        for (int i = 0; i < numNodes; i++) {
            float[] vec = new float[dim];
            for (int d = 0; d < dim; d++) {
                vec[d] = (float) rng.nextGaussian();
            }
            vec = DistanceFunction.normalize(vec);
            vectors.add(vec);
            index.insert("vec_" + i, vec, Collections.emptyMap());
        }

        assertEquals(numNodes, index.size());

        // Test recall across 20 independent queries
        int totalHits = 0;
        int totalPossible = 20 * k;

        for (int q = 0; q < 20; q++) {
            float[] query = new float[dim];
            for (int d = 0; d < dim; d++) {
                query[d] = (float) rng.nextGaussian();
            }
            query = DistanceFunction.normalize(query);

            List<SearchResult> hnswResults = index.search(query, k, 64, null);
            List<SearchResult> groundTruth = index.bruteForceSearch(query, k, null);

            Set<String> trueIds = new HashSet<>();
            for (SearchResult sr : groundTruth) {
                trueIds.add(sr.getNode().getId());
            }

            for (SearchResult sr : hnswResults) {
                if (trueIds.contains(sr.getNode().getId())) {
                    totalHits++;
                }
            }
        }

        double recall = (double) totalHits / totalPossible;
        assertTrue(recall >= 0.90, "Recall@5 should be >= 90%, was: " + (recall * 100.0) + "%");
    }

    @Test
    void testFilteredSearch() {
        int dim = 8;
        HnswGraphIndex index = new HnswGraphIndex(dim, DistanceMetric.EUCLIDEAN, 16, 64, 32, false);

        for (int i = 0; i < 50; i++) {
            float[] vec = new float[dim];
            Arrays.fill(vec, (float) i);
            String cat = (i % 2 == 0) ? "even" : "odd";
            index.insert("item_" + i, vec, Map.of("parity", cat));
        }

        float[] query = new float[dim];
        Arrays.fill(query, 10.0f);

        // Search with filter parity == odd
        List<SearchResult> results = index.search(query, 5, 32, node -> "odd".equals(node.getMetadata().get("parity")));

        assertFalse(results.isEmpty());
        for (SearchResult sr : results) {
            assertEquals("odd", sr.getNode().getMetadata().get("parity"));
        }
    }
}
