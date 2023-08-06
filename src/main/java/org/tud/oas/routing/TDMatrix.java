package org.tud.oas.routing;

public class TDMatrix implements ITDMatrix {
    private double[][] durations;

    public TDMatrix(double[][] durations) {
        this.durations = durations;
    }

    public float getRange(int source, int destination) {
        return (float) durations[source][destination];
    }
}