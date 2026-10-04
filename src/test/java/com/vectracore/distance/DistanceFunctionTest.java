package com.vectracore.distance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DistanceFunctionTest {

    @Test
    void testCosineDistanceIdenticalVectors() {
        float[] a = {1.0f, 2.0f, 3.0f};
        float[] b = {1.0f, 2.0f, 3.0f};
        float dist = DistanceFunction.cosineDistance(a, b);
        assertEquals(0.0f, dist, 1e-5f);
    }

    @Test
    void testCosineDistanceOrthogonalVectors() {
        float[] a = {1.0f, 0.0f, 0.0f};
        float[] b = {0.0f, 1.0f, 0.0f};
        float dist = DistanceFunction.cosineDistance(a, b);
        assertEquals(1.0f, dist, 1e-5f);
    }

    @Test
    void testCosineDistanceOppositeVectors() {
        float[] a = {1.0f, 0.0f, 0.0f};
        float[] b = {-1.0f, 0.0f, 0.0f};
        float dist = DistanceFunction.cosineDistance(a, b);
        assertEquals(2.0f, dist, 1e-5f);
    }

    @Test
    void testEuclideanDistance() {
        float[] a = {0.0f, 0.0f};
        float[] b = {3.0f, 4.0f};
        float dist = DistanceFunction.euclideanDistance(a, b);
        assertEquals(5.0f, dist, 1e-5f);
    }

    @Test
    void testDotProduct() {
        float[] a = {2.0f, 3.0f, 4.0f};
        float[] b = {1.0f, 2.0f, 3.0f};
        float dot = DistanceFunction.dotProduct(a, b);
        assertEquals(20.0f, dot, 1e-5f); // 2*1 + 3*2 + 4*3 = 2 + 6 + 12 = 20
    }

    @Test
    void testNormalize() {
        float[] v = {3.0f, 4.0f};
        float[] norm = DistanceFunction.normalize(v);
        assertEquals(0.6f, norm[0], 1e-5f);
        assertEquals(0.8f, norm[1], 1e-5f);
        float magnitude = (float) Math.sqrt(norm[0] * norm[0] + norm[1] * norm[1]);
        assertEquals(1.0f, magnitude, 1e-5f);
    }
}
