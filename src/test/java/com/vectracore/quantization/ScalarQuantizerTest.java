package com.vectracore.quantization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScalarQuantizerTest {

    @Test
    void testQuantizationAndDequantizationAccuracy() {
        float[] original = {0.1f, -0.5f, 0.8f, 1.2f, -1.0f};
        ScalarQuantizer.QuantizedVector q = ScalarQuantizer.quantize(original);

        assertNotNull(q.getData());
        assertEquals(original.length, q.getData().length);

        float[] restored = q.dequantize();
        assertEquals(original.length, restored.length);

        // Max error for 8-bit scalar quantization is (max - min) / 255
        float maxError = (1.2f - (-1.0f)) / 255.0f;
        for (int i = 0; i < original.length; i++) {
            assertEquals(original[i], restored[i], maxError + 0.005f);
        }
    }

    @Test
    void testQuantizedDistanceApproximation() {
        float[] a = {1.0f, 2.0f, 3.0f, 4.0f};
        float[] b = {1.1f, 2.2f, 2.9f, 3.8f};

        ScalarQuantizer.QuantizedVector qa = ScalarQuantizer.quantize(a);
        ScalarQuantizer.QuantizedVector qb = ScalarQuantizer.quantize(b);

        float distExact = 0.0f;
        for (int i = 0; i < a.length; i++) {
            float d = a[i] - b[i];
            distExact += d * d;
        }
        distExact = (float) Math.sqrt(distExact);

        float distApprox = ScalarQuantizer.quantizedEuclideanDistance(qa, qb);
        assertEquals(distExact, distApprox, 0.08f);
    }
}
