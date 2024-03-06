package org.tud.oas.accessibility.distance_decay;

public class KernelDensityDecay implements IDistanceDecay {
    float max_distance;
    float factor = (float) 0.75;

    public KernelDensityDecay(float max_distance) {
        this.max_distance = max_distance;
    }

    public float getDistanceWeight(float distance) {
        if (distance > max_distance) {
            return 0;
        } else {
            return factor * (1 - (float) Math.pow(distance / max_distance, 2));
        }
    }

    public float getMaxDistance() {
        return this.max_distance;
    }

    public float[] getDistances() {
        return null;
    }
}
