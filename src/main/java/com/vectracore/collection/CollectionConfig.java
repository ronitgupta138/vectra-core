package com.vectracore.collection;

import com.vectracore.distance.DistanceMetric;

public class CollectionConfig {
    private final String name;
    private final int dimension;
    private final DistanceMetric metric;
    private final int m;
    private final int efConstruction;
    private final int efSearch;
    private final boolean useQuantization;

    public CollectionConfig(String name, int dimension, DistanceMetric metric,
                            int m, int efConstruction, int efSearch, boolean useQuantization) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Dimension must be positive: " + dimension);
        }
        this.name = name;
        this.dimension = dimension;
        this.metric = metric != null ? metric : DistanceMetric.COSINE;
        this.m = m > 0 ? m : 16;
        this.efConstruction = efConstruction > 0 ? efConstruction : 64;
        this.efSearch = efSearch > 0 ? efSearch : 32;
        this.useQuantization = useQuantization;
    }

    public String getName() { return name; }
    public int getDimension() { return dimension; }
    public DistanceMetric getMetric() { return metric; }
    public int getM() { return m; }
    public int getEfConstruction() { return efConstruction; }
    public int getEfSearch() { return efSearch; }
    public boolean isUseQuantization() { return useQuantization; }
}
