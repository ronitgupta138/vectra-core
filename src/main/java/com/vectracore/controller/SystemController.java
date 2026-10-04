package com.vectracore.controller;

import com.vectracore.dto.SystemStatsResponse;
import com.vectracore.service.VectorEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    private final VectorEngineService engineService;

    public SystemController(VectorEngineService engineService) {
        this.engineService = engineService;
    }

    @GetMapping("/stats")
    public ResponseEntity<SystemStatsResponse> getSystemStats() {
        return ResponseEntity.ok(engineService.getSystemStats());
    }
}
