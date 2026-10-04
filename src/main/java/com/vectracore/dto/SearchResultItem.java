package com.vectracore.dto;

import java.util.Map;

public class SearchResultItem {
    private final String id;
    private final float distance;
    private final float similarity;
    private final Map<String, Object> metadata;

    public SearchResultItem(String id, float distance, float similarity, Map<String, Object> metadata) {
        this.id = id;
        this.distance = Math.round(distance * 10000.0f) / 10000.0f;
        this.similarity = Math.round(similarity * 10000.0f) / 10000.0f;
        this.metadata = metadata;
    }

    public String getId() { return id; }
    public float getDistance() { return distance; }
    public float getSimilarity() { return similarity; }
    public Map<String, Object> getMetadata() { return metadata; }
}
