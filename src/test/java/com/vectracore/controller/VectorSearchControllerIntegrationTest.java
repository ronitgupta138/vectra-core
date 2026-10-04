package com.vectracore.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VectorSearchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testPreloadedDemoCollection() throws Exception {
        mockMvc.perform(get("/api/v1/collections/vision-embeddings-128"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("vision-embeddings-128")))
                .andExpect(jsonPath("$.dimension", is(128)))
                .andExpect(jsonPath("$.vectorCount", greaterThanOrEqualTo(2000)))
                .andExpect(jsonPath("$.metric", is("COSINE")));
    }

    @Test
    void testCreateCollectionAndVectorSearchLifecycle() throws Exception {
        String createJson = """
                {
                    "name": "ecommerce-catalog",
                    "dimension": 4,
                    "metric": "EUCLIDEAN",
                    "m": 16,
                    "efConstruction": 64,
                    "efSearch": 32,
                    "useQuantization": false
                }
                """;

        mockMvc.perform(post("/api/v1/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("ecommerce-catalog")))
                .andExpect(jsonPath("$.dimension", is(4)));

        // Insert vector
        String insertJson = """
                {
                    "id": "prod_1",
                    "vector": [1.0, 2.0, 3.0, 4.0],
                    "metadata": {"brand": "Nike", "category": "shoes"}
                }
                """;

        mockMvc.perform(post("/api/v1/collections/ecommerce-catalog/insert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(insertJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("prod_1")))
                .andExpect(jsonPath("$.status", is("INSERTED")));

        // Search vector
        String searchJson = """
                {
                    "vector": [1.05, 2.02, 2.98, 4.01],
                    "k": 5
                }
                """;

        mockMvc.perform(post("/api/v1/collections/ecommerce-catalog/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(searchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection", is("ecommerce-catalog")))
                .andExpect(jsonPath("$.returned", greaterThan(0)))
                .andExpect(jsonPath("$.results[0].id", is("prod_1")))
                .andExpect(jsonPath("$.executionTimeMs", notNullValue()));
    }

    @Test
    void testDimensionMismatchValidation() throws Exception {
        String badInsertJson = """
                {
                    "id": "bad_vec",
                    "vector": [1.0, 2.0]
                }
                """;

        mockMvc.perform(post("/api/v1/collections/vision-embeddings-128/insert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badInsertJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Dimension Mismatch")));
    }

    @Test
    void testSystemStatsEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/system/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCollections", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalVectors", greaterThanOrEqualTo(2000)))
                .andExpect(jsonPath("$.memoryUsageMb", notNullValue()));
    }
}
