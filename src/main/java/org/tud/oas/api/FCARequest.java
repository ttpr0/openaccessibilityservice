package org.tud.oas.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FCARequest {
    @JsonProperty("ranges")
    private List<Double> ranges;

    @JsonProperty("range_factors")
    private List<Double> factors;

    @JsonProperty("facility_locations")
    private Double[][] locations;

    public List<Double> getRanges() {
        return ranges;
    }

    public void setRanges(List<Double> ranges) {
        this.ranges = ranges;
    }

    public List<Double> getFactors() {
        return factors;
    }

    public void setFactors(List<Double> factors) {
        this.factors = factors;
    }

    public Double[][] getLocations() {
        return locations;
    }

    public void setLocations(Double[][] locations) {
        this.locations = locations;
    }
}
