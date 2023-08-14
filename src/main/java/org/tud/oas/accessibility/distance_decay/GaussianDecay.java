package org.tud.oas.accessibility.distance_decay;

public class GaussianDecay implements IDistanceDecay {
    float max_distance;
    float impedance;

    public GaussianDecay(float max_distance) {
        this.max_distance = max_distance;
        this.impedance = (float) (-Math.pow(max_distance, 2) / Math.log(0.01));
    }

    public float getDistanceWeight(float distance) {
        if (distance >= max_distance) {
            return 0;
        } else {
            return (float) Math.exp(-Math.pow(distance, 2) / impedance);
        }
    }

    public float getMaxDistance() {
        return this.max_distance;
    }

    public float[] getDistances() {
        return null;
    }
}
