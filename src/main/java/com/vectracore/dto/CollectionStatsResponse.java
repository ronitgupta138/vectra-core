package com.vectracore.dto;

import com.vectracore.distance.DistanceMetric;

public class CollectionStatsResponse {
    private final String name;
    private final int dimension;
    private final DistanceMetric metric;
    private final int vectorCount;
    private final int maxGraphLevel;
    private final long totalQueries;
    private final boolean useQuantization;
    private final long createdAtEpoch;

    public CollectionStatsResponse(String name, int dimension, DistanceMetric metric,
                                   int vectorCount, int maxGraphLevel, long totalQueries,
                                   boolean useQuantization, long createdAtEpoch) {
        this.name = name;
        this.dimension = dimension;
        this.metric = metric;
        this.vectorCount = vectorCount;
        this.maxGraphLevel = maxGraphLevel;
        this.totalQueries = totalQueries;
        this.useQuantization = useQuantization;
        this.createdAtEpoch = createdAtEpoch;
    }

    public String getName() { return name; }
    public int getDimension() { return dimension; }
    public DistanceMetric getMetric() { return metric; }
    public int getVectorCount() { return vectorCount; }
    public int getMaxGraphLevel() { return maxGraphLevel; }
    public long getTotalQueries() { return totalQueries; }
    public boolean isUseQuantization() { return useQuantization; }
    public long getCreatedAtEpoch() { return createdAtEpoch; }
}
