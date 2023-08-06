package org.tud.oas.accessibility.distance_decay;

public class InversePowerDecay implements IDistanceDecay {
    float max_distance;
    float impedance;

    public InversePowerDecay(float max_distance) {
        this.max_distance = max_distance;
        this.impedance = (float) (Math.log(1 / 0.01) / Math.log(max_distance));
    }

    public float getDistanceWeight(float distance) {
        if (distance >= max_distance) {
            return 0;
        } else {
            return (float) Math.pow(distance, -impedance);
        }
    }
}
