package org.tud.oas.accessibility.distance_decay;

public class ExponentialDecay implements IDistanceDecay {
    float max_distance;
    float impedance;

    public ExponentialDecay(float max_distance) {
        this.max_distance = max_distance;
        this.impedance = (float) (-Math.log(0.01) / max_distance);
    }

    public float getDistanceWeight(float distance) {
        if (distance > max_distance) {
            return 0;
        } else {
            return (float) Math.exp(-distance * impedance);
        }
    }

    public float getMaxDistance() {
        return this.max_distance;
    }

    public float[] getDistances() {
        return null;
    }
}
