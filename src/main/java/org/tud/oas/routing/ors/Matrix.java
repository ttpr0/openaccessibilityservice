package org.tud.oas.routing.ors;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Matrix {
    @JsonProperty("durations")
    private double[][] durations;
    @JsonProperty("distances")
    private double[][] distances;
    private String error;

    public Matrix(double[][] durations, double[][] distances) {
        this.durations = durations;
        this.distances = distances;
        this.error = null;
    }

    public Matrix(String error) {
        this.error = error;
        this.distances = null;
        this.durations = null;
    }

    public boolean hasDurations() {
        return this.durations != null;
    }

    public double[][] getDurations() {
        return this.durations;
    }

    public boolean hasDistances() {
        return this.distances != null;
    }

    public double[][] getDistances() {
        return this.distances;
    }

    public boolean isNull() {
        return this.error != null;
    }

    public String getError() {
        return this.error;
    }
}