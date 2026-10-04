package com.vectracore.quantization;

public class ScalarQuantizer {

    public static class QuantizedVector {
        private final byte[] data;
        private final float min;
        private final float scale; // (max - min) / 255.0

        public QuantizedVector(byte[] data, float min, float scale) {
            this.data = data;
            this.min = min;
            this.scale = scale;
        }

        public byte[] getData() { return data; }
        public float getMin() { return min; }
        public float getScale() { return scale; }

        public float[] dequantize() {
            float[] result = new float[data.length];
            for (int i = 0; i < data.length; i++) {
                int unsigned = data[i] & 0xFF;
                result[i] = min + (unsigned * scale);
            }
            return result;
        }
    }

    public static QuantizedVector quantize(float[] vector) {
        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException("Vector cannot be null or empty");
        }

        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;

        for (float v : vector) {
            if (v < min) min = v;
            if (v > max) max = v;
        }

        float range = max - min;
        float scale = (range == 0.0f) ? 1.0f : range / 255.0f;

        byte[] quantized = new byte[vector.length];
        for (int i = 0; i < vector.length; i++) {
            int q = Math.round((vector[i] - min) / scale);
            quantized[i] = (byte) Math.min(255, Math.max(0, q));
        }

        return new QuantizedVector(quantized, min, scale);
    }

    /**
     * Approximates Euclidean distance between two quantized vectors using fast integer arithmetic.
     */
    public static float quantizedEuclideanDistance(QuantizedVector q1, QuantizedVector q2) {
        byte[] d1 = q1.data;
        byte[] d2 = q2.data;
        float s1 = q1.scale;
        float s2 = q2.scale;
        float m1 = q1.min;
        float m2 = q2.min;

        float sum = 0.0f;
        for (int i = 0; i < d1.length; i++) {
            float v1 = m1 + ((d1[i] & 0xFF) * s1);
            float v2 = m2 + ((d2[i] & 0xFF) * s2);
            float diff = v1 - v2;
            sum += diff * diff;
        }
        return (float) Math.sqrt(sum);
    }
}
