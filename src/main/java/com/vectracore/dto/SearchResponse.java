package com.vectracore.dto;

import java.util.List;

public class SearchResponse {
    private final String collection;
    private final int k;
    private final int returned;
    private final List<SearchResultItem> results;
    private final double executionTimeMs;

    public SearchResponse(String collection, int k, List<SearchResultItem> results, double executionTimeMs) {
        this.collection = collection;
        this.k = k;
        this.results = results;
        this.returned = results != null ? results.size() : 0;
        this.executionTimeMs = Math.round(executionTimeMs * 1000.0) / 1000.0;
    }

    public String getCollection() { return collection; }
    public int getK() { return k; }
    public int getReturned() { return returned; }
    public List<SearchResultItem> getResults() { return results; }
    public double getExecutionTimeMs() { return executionTimeMs; }
}
