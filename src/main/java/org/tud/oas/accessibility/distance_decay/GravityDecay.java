package org.tud.oas.accessibility.distance_decay;

public class GravityDecay implements IDistanceDecay {
    float max_distance;
    float beta;

    public GravityDecay(float max_distance, float beta) {
        this.max_distance = max_distance;
        this.beta = beta;
    }

    public float getDistanceWeight(float distance) {
        if (distance >= max_distance) {
            return 0;
        } else {
            return (float) Math.pow(distance, -beta);
        }
    }
}
