package com.vectracore.service;

import com.vectracore.distance.DistanceFunction;
import com.vectracore.dto.SearchResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VectorEngineBenchmarkTest {
    private static final Logger log = LoggerFactory.getLogger(VectorEngineBenchmarkTest.class);

    @Autowired
    private VectorEngineService engineService;

    @Test
    void benchmarkHighConcurrencyVirtualThreads() throws Exception {
        String collection = "vision-embeddings-128";
        int dim = 128;
        int totalRequests = 1000;

        Random rng = new Random(42);

        // Warm-up phase to trigger HotSpot C2 compiler
        for (int i = 0; i < 300; i++) {
            float[] wVec = new float[dim];
            for (int d = 0; d < dim; d++) wVec[d] = (float) rng.nextGaussian();
            wVec = DistanceFunction.normalize(wVec);
            engineService.search(collection, wVec, 10, 32, null);
        }

        long[] latenciesNanos = new long[totalRequests];
        long benchStart = System.currentTimeMillis();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>(totalRequests);

            for (int i = 0; i < totalRequests; i++) {
                final int idx = i;
                float[] qVec = new float[dim];
                for (int d = 0; d < dim; d++) qVec[d] = (float) rng.nextGaussian();
                final float[] normQuery = DistanceFunction.normalize(qVec);

                futures.add(executor.submit(() -> {
                    long t0 = System.nanoTime();
                    SearchResponse res = engineService.search(collection, normQuery, 10, 32, null);
                    latenciesNanos[idx] = System.nanoTime() - t0;
                    assertNotNull(res);
                    assertEquals(10, res.getReturned());
                }));
            }

            for (Future<?> f : futures) {
                f.get(10, TimeUnit.SECONDS);
            }
        }

        long totalBenchDurationMs = System.currentTimeMillis() - benchStart;

        double[] latenciesMs = new double[totalRequests];
        for (int i = 0; i < totalRequests; i++) {
            latenciesMs[i] = latenciesNanos[i] / 1_000_000.0;
        }
        Arrays.sort(latenciesMs);

        double p50 = latenciesMs[(int) (totalRequests * 0.50)];
        double p95 = latenciesMs[(int) (totalRequests * 0.95)];
        double p99 = latenciesMs[(int) (totalRequests * 0.99)];
        double throughputQps = (totalRequests / (double) totalBenchDurationMs) * 1000.0;

        log.info("==================================================================");
        log.info("VECTRA-CORE 128-DIM HNSW CONCURRENCY BENCHMARK (Java 21 Loom):");
        log.info("  - Total Executed Queries : {}", totalRequests);
        log.info("  - Total Test Duration    : {} ms", totalBenchDurationMs);
        log.info("  - Throughput             : {} QPS", String.format("%.2f", throughputQps));
        log.info("  - Latency p50 (Median)   : {} ms", String.format("%.3f", p50));
        log.info("  - Latency p95            : {} ms", String.format("%.3f", p95));
        log.info("  - Latency p99            : {} ms", String.format("%.3f", p99));
        log.info("==================================================================");

        assertTrue(p99 < 15.0, "p99 latency should be strictly under 15ms, was: " + p99 + "ms");
        assertTrue(p50 < 1.0, "p50 median latency should be strictly under 1ms, was: " + p50 + "ms");
    }
}
