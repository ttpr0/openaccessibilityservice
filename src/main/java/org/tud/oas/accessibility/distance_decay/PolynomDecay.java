package org.tud.oas.accessibility.distance_decay;

public class PolynomDecay implements IDistanceDecay {
    float max_distance;

    float[] factors;

    public PolynomDecay(float max_distance, float[] factors) {
        this.max_distance = max_distance;
        this.factors = factors;
    }

    public float getDistanceWeight(float distance) {
        if (distance >= max_distance) {
            return 0;
        }
        int degree = factors.length - 1;
        double weight = factors[degree];
        for (int i = 0; i < degree; i++) {
            weight += Math.pow(distance, degree - i) * factors[i];
        }
        return (float) weight;
    }
}
