package org.tud.oas.routing;

public class NNTable implements INNTable {
    private int[] nearest;
    private float[] ranges;

    public NNTable(int[] nearest, float[] ranges) {
        this.nearest = nearest;
        this.ranges = ranges;
    }

    public int getNearest(int destination) {
        return this.nearest[destination];
    }

    public float getNearestRange(int destination) {
        return this.ranges[destination];
    }
}