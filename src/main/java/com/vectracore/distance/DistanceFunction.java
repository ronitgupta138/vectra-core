package com.vectracore.distance;

public final class DistanceFunction {

    private DistanceFunction() {}

    public static float compute(DistanceMetric metric, float[] a, float[] b) {
        return switch (metric) {
            case COSINE -> cosineDistance(a, b);
            case EUCLIDEAN -> euclideanDistance(a, b);
            case DOT_PRODUCT -> -dotProduct(a, b); // Negate so smaller distance = higher similarity
        };
    }

    public static float cosineDistance(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Dimension mismatch: " + a.length + " vs " + b.length);
        }
        float dot = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;

        // Loop structured for HotSpot C2 autovectorization (SIMD)
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0.0f || normB == 0.0f) {
            return 1.0f;
        }

        float similarity = (float) (dot / (Math.sqrt(normA) * Math.sqrt(normB)));
        // Clamp to [-1.0, 1.0] to prevent floating point drift outside domain
        similarity = Math.max(-1.0f, Math.min(1.0f, similarity));
        return 1.0f - similarity;
    }

    public static float euclideanDistance(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Dimension mismatch: " + a.length + " vs " + b.length);
        }
        float sum = 0.0f;
        for (int i = 0; i < a.length; i++) {
            float diff = a[i] - b[i];
            sum += diff * diff;
        }
        return (float) Math.sqrt(sum);
    }

    public static float dotProduct(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Dimension mismatch: " + a.length + " vs " + b.length);
        }
        float dot = 0.0f;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
        }
        return dot;
    }

    public static float[] normalize(float[] v) {
        float sum = 0.0f;
        for (float val : v) {
            sum += val * val;
        }
        float norm = (float) Math.sqrt(sum);
        if (norm == 0.0f) return v.clone();

        float[] result = new float[v.length];
        for (int i = 0; i < v.length; i++) {
            result[i] = v[i] / norm;
        }
        return result;
    }
}
