package org.tud.oas.routing;

public class KNNTable implements IKNNTable {
    private int[][] nearest;
    private float[][] ranges;

    public KNNTable(int[][] nearest, float[][] ranges) {
        this.nearest = nearest;
        this.ranges = ranges;
    }

    public int getKNearest(int destination, int k) {
        return this.nearest[destination][k];
    }

    public float getKNearestRange(int destination, int k) {
        return this.ranges[destination][k];
    }
}