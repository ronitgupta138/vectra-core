package com.vectracore.dto;

import java.util.List;

public class SystemStatsResponse {
    private final int totalCollections;
    private final int totalVectors;
    private final String memoryUsageMb;
    private final int availableProcessors;
    private final boolean virtualThreadsEnabled;
    private final List<String> collectionNames;

    public SystemStatsResponse(int totalCollections, int totalVectors, String memoryUsageMb,
                               int availableProcessors, boolean virtualThreadsEnabled,
                               List<String> collectionNames) {
        this.totalCollections = totalCollections;
        this.totalVectors = totalVectors;
        this.memoryUsageMb = memoryUsageMb;
        this.availableProcessors = availableProcessors;
        this.virtualThreadsEnabled = virtualThreadsEnabled;
        this.collectionNames = collectionNames;
    }

    public int getTotalCollections() { return totalCollections; }
    public int getTotalVectors() { return totalVectors; }
    public String getMemoryUsageMb() { return memoryUsageMb; }
    public int getAvailableProcessors() { return availableProcessors; }
    public boolean isVirtualThreadsEnabled() { return virtualThreadsEnabled; }
    public List<String> getCollectionNames() { return collectionNames; }
}
