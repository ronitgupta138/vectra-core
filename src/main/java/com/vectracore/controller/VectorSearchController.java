package com.vectracore.controller;

import com.vectracore.dto.BatchInsertRequest;
import com.vectracore.dto.InsertVectorRequest;
import com.vectracore.dto.SearchResponse;
import com.vectracore.dto.VectorSearchRequest;
import com.vectracore.service.VectorEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/collections/{name}")
public class VectorSearchController {

    private final VectorEngineService engineService;

    public VectorSearchController(VectorEngineService engineService) {
        this.engineService = engineService;
    }

    @PostMapping("/insert")
    public ResponseEntity<Map<String, Object>> insertVector(
            @PathVariable("name") String name,
            @RequestBody InsertVectorRequest request) {
        engineService.insert(name, request.getId(), request.getVector(), request.getMetadata());
        return ResponseEntity.ok(Map.of("collection", name, "id", request.getId(), "status", "INSERTED"));
    }

    @PostMapping("/batch-insert")
    public ResponseEntity<Map<String, Object>> batchInsert(
            @PathVariable("name") String name,
            @RequestBody BatchInsertRequest request) {
        int inserted = engineService.batchInsert(name, request.getRecords());
        return ResponseEntity.ok(Map.of("collection", name, "insertedCount", inserted, "status", "SUCCESS"));
    }

    @PostMapping("/search")
    public ResponseEntity<SearchResponse> search(
            @PathVariable("name") String name,
            @RequestBody VectorSearchRequest request) {
        SearchResponse response = engineService.search(
                name,
                request.getVector(),
                request.getK(),
                request.getEf(),
                request.getFilter()
        );
        return ResponseEntity.ok(response);
    }
}
