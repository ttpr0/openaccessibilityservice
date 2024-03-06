package org.tud.oas.accessibility.distance_decay;

public class LinearDecay implements IDistanceDecay {
    float max_distance;

    public LinearDecay(float max_distance) {
        this.max_distance = max_distance;
    }

    public float getDistanceWeight(float distance) {
        if (distance > max_distance) {
            return 0;
        } else {
            return 1 - (distance / max_distance);
        }
    }

    public float getMaxDistance() {
        return this.max_distance;
    }

    public float[] getDistances() {
        return null;
    }
}