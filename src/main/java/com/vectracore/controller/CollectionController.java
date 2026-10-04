package com.vectracore.controller;

import com.vectracore.dto.CollectionStatsResponse;
import com.vectracore.dto.CreateCollectionRequest;
import com.vectracore.service.VectorEngineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/collections")
public class CollectionController {

    private final VectorEngineService engineService;

    public CollectionController(VectorEngineService engineService) {
        this.engineService = engineService;
    }

    @PostMapping
    public ResponseEntity<CollectionStatsResponse> createCollection(@RequestBody CreateCollectionRequest request) {
        CollectionStatsResponse response = engineService.createCollection(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{name}")
    public ResponseEntity<CollectionStatsResponse> getCollection(@PathVariable("name") String name) {
        return ResponseEntity.ok(engineService.getCollectionStats(name));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Map<String, Object>> deleteCollection(@PathVariable("name") String name) {
        boolean deleted = engineService.deleteCollection(name);
        return ResponseEntity.ok(Map.of("collection", name, "deleted", deleted));
    }
}
