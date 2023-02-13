package org.tud.oas.api.accessibility;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccessibilityRequest {
    @JsonProperty("ranges")
    private List<Double> ranges;

    @JsonProperty("facility_locations")
    private Double[][] locations;

    public List<Double> getRanges() {
        return ranges;
    }

    public void setRanges(List<Double> ranges) {
        this.ranges = ranges;
    }

    public Double[][] getLocations() {
        return locations;
    }

    public void setLocations(Double[][] locations) {
        this.locations = locations;
    }
}
