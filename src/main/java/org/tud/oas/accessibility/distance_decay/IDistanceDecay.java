package org.tud.oas.accessibility.distance_decay;

public interface IDistanceDecay {
    public float getDistanceWeight(float distance);

    /**
     * Returns the maximum distance threshold.
     * Distances higher than this get weight 0.
     * 
     * @return maximum distance
     */
    public float getMaxDistance();

    /**
     * Returns the internally used distances (currently only hybrid decay)
     * 
     * @return distances (if available) or null
     */
    public float[] getDistances();
}
