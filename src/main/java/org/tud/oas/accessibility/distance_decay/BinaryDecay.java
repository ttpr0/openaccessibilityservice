package org.tud.oas.accessibility.distance_decay;

public class BinaryDecay implements IDistanceDecay {
    float max_distance;
    float factor = (float) 1;

    public BinaryDecay(float max_distance) {
        this.max_distance = max_distance;
    }

    public float getDistanceWeight(float distance) {
        if (distance > max_distance) {
            return 0;
        } else {
            return factor;
        }
    }

    public float getMaxDistance() {
        return this.max_distance;
    }

    public float[] getDistances() {
        return null;
    }
}