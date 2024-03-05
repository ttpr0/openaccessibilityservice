package org.tud.oas.routing;

import java.util.List;
import java.util.ArrayList;

public class RoutingOptions {
    private String mode = "isochrones";
    private List<Double> ranges;
    private Double max_range;
    private Integer range_steps;

    public RoutingOptions() {
    }

    public RoutingOptions(String mode) {
        this.mode = mode;
    }

    public RoutingOptions(String mode, List<Double> ranges) {
        this.mode = mode;
        this.ranges = ranges;
    }

    public RoutingOptions(String mode, Double max_range, Integer range_steps) {
        this.mode = mode;
        this.max_range = max_range;
        this.range_steps = range_steps;
    }

    public RoutingOptions(String mode, Double max_range) {
        this.mode = mode;
        this.max_range = max_range;
        this.ranges = new ArrayList<Double>();
        this.ranges.add(max_range);
    }

    public String getMode() {
        return this.mode;
    }

    public List<Double> getRanges() {
        return ranges;
    }

    public boolean hasRanges() {
        return this.ranges != null;
    }

    public Double getMaxRange() {
        return max_range;
    }

    public boolean hasMaxRange() {
        return this.max_range != null;
    }

    public Integer getRangeSteps() {
        return range_steps;
    }

    public boolean hasRangeSteps() {
        return this.range_steps != null;
    }
}
