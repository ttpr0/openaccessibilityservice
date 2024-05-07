package org.tud.oas.accessibility.distance_decay;

import java.util.Arrays;

public class PiecewiseLinearDecay implements IDistanceDecay {
    float[] distances;
    float[] factors;

    public PiecewiseLinearDecay(float[] distances, float[] factors) {
        // sort two arrays using Pair helper class
        // sorted based on distances (lowest first)
        Pair[] pairs = new Pair[distances.length];
        for (int i = 0; i < distances.length; i++) {
            pairs[i] = new Pair(distances[i], factors[i]);
        }
        Arrays.sort(pairs);
        for (int i = 0; i < pairs.length; i++) {
            distances[i] = pairs[i].value1;
            factors[i] = pairs[i].value2;
        }

        this.distances = distances;
        this.factors = factors;
    }

    public float getDistanceWeight(float distance) {
        for (int i = 1; i < this.distances.length; i++) {
            if (distance <= this.distances[i]) {
                float m = (this.factors[i] - this.factors[i - 1]) / (this.distances[i] - this.distances[i - 1]);
                return m * (distance - this.distances[i - 1]) + this.factors[i - 1];
            }
        }
        return 0;
    }

    public float getMaxDistance() {
        return this.distances[this.distances.length - 1];
    }

    public float[] getDistances() {
        return null;
    }
}
