package com.vectracore.dto;

import java.util.Map;

public class InsertVectorRequest {
    private String id;
    private float[] vector;
    private Map<String, Object> metadata;

    public InsertVectorRequest() {}

    public InsertVectorRequest(String id, float[] vector, Map<String, Object> metadata) {
        this.id = id;
        this.vector = vector;
        this.metadata = metadata;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public float[] getVector() { return vector; }
    public void setVector(float[] vector) { this.vector = vector; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
