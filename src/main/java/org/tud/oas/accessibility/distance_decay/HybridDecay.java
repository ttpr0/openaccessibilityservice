package org.tud.oas.accessibility.distance_decay;

import java.util.Arrays;

public class HybridDecay implements IDistanceDecay {
    float[] distances;
    float[] factors;

    public HybridDecay(float[] distances, float[] factors) {
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
        for (int i = 0; i < distances.length; i++) {
            if (distance <= distances[i]) {
                return factors[i];
            }
        }
        return 0;
    }

    public float getMaxDistance() {
        return this.distances[this.distances.length - 1];
    }

    public float[] getDistances() {
        return this.distances;
    }
}

class Pair implements Comparable<Pair> {
    float value1;
    float value2;

    public Pair(float value1, float value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public int compareTo(Pair other) {
        return Float.compare(this.value1, other.value1);
    }
}