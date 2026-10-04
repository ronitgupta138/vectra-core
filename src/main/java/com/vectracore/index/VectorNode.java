package com.vectracore.index;

import com.vectracore.quantization.ScalarQuantizer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VectorNode {
    private final String id;
    private final float[] vector;
    private final ScalarQuantizer.QuantizedVector quantized;
    private final Map<String, Object> metadata;
    private final int level;
    private final Map<Integer, Set<VectorNode>> neighborsByLevel = new ConcurrentHashMap<>();

    public VectorNode(String id, float[] vector, Map<String, Object> metadata, int level, boolean quantize) {
        this.id = id;
        this.vector = vector;
        this.metadata = metadata != null ? new HashMap<>(metadata) : Collections.emptyMap();
        this.level = level;
        this.quantized = quantize ? ScalarQuantizer.quantize(vector) : null;

        for (int l = 0; l <= level; l++) {
            neighborsByLevel.put(l, Collections.newSetFromMap(new ConcurrentHashMap<>()));
        }
    }

    public String getId() { return id; }
    public float[] getVector() { return vector; }
    public ScalarQuantizer.QuantizedVector getQuantized() { return quantized; }
    public Map<String, Object> getMetadata() { return metadata; }
    public int getLevel() { return level; }

    public Set<VectorNode> getNeighbors(int l) {
        return neighborsByLevel.computeIfAbsent(l, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
    }

    public void addNeighbor(int l, VectorNode neighbor) {
        getNeighbors(l).add(neighbor);
    }

    public void removeNeighbor(int l, VectorNode neighbor) {
        getNeighbors(l).remove(neighbor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VectorNode that = (VectorNode) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "VectorNode{id='" + id + "', dim=" + vector.length + ", level=" + level + "}";
    }
}
