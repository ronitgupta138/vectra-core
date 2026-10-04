package com.vectracore.dto;

import com.vectracore.distance.DistanceMetric;

public class CreateCollectionRequest {
    private String name;
    private int dimension;
    private DistanceMetric metric = DistanceMetric.COSINE;
    private Integer m = 16;
    private Integer efConstruction = 64;
    private Integer efSearch = 32;
    private Boolean useQuantization = false;

    public CreateCollectionRequest() {}

    public CreateCollectionRequest(String name, int dimension, DistanceMetric metric) {
        this.name = name;
        this.dimension = dimension;
        this.metric = metric;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDimension() { return dimension; }
    public void setDimension(int dimension) { this.dimension = dimension; }

    public DistanceMetric getMetric() { return metric; }
    public void setMetric(DistanceMetric metric) { this.metric = metric; }

    public Integer getM() { return m; }
    public void setM(Integer m) { this.m = m; }

    public Integer getEfConstruction() { return efConstruction; }
    public void setEfConstruction(Integer efConstruction) { this.efConstruction = efConstruction; }

    public Integer getEfSearch() { return efSearch; }
    public void setEfSearch(Integer efSearch) { this.efSearch = efSearch; }

    public Boolean getUseQuantization() { return useQuantization; }
    public void setUseQuantization(Boolean useQuantization) { this.useQuantization = useQuantization; }
}
