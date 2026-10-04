package com.vectracore.index;

public class SearchResult implements Comparable<SearchResult> {
    private final VectorNode node;
    private final float distance;
    private final float similarity;

    public SearchResult(VectorNode node, float distance) {
        this.node = node;
        this.distance = distance;
        // For cosine distance in [0, 2], similarity = 1.0 - (distance / 2.0)
        // For euclidean, 1.0 / (1.0 + distance)
        this.similarity = Math.max(0.0f, 1.0f / (1.0f + distance));
    }

    public SearchResult(VectorNode node, float distance, float similarity) {
        this.node = node;
        this.distance = distance;
        this.similarity = similarity;
    }

    public VectorNode getNode() { return node; }
    public float getDistance() { return distance; }
    public float getSimilarity() { return similarity; }

    @Override
    public int compareTo(SearchResult o) {
        return Float.compare(this.distance, o.distance);
    }
}
