package com.vectracore.dto;

import java.util.Map;

public class VectorSearchRequest {
    private float[] vector;
    private int k = 10;
    private Integer ef;
    private Map<String, Object> filter;

    public VectorSearchRequest() {}

    public VectorSearchRequest(float[] vector, int k) {
        this.vector = vector;
        this.k = k;
    }

    public float[] getVector() { return vector; }
    public void setVector(float[] vector) { this.vector = vector; }

    public int getK() { return k; }
    public void setK(int k) { this.k = k; }

    public Integer getEf() { return ef; }
    public void setEf(Integer ef) { this.ef = ef; }

    public Map<String, Object> getFilter() { return filter; }
    public void setFilter(Map<String, Object> filter) { this.filter = filter; }
}
