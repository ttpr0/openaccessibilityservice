package org.tud.oas.routing;

import java.util.List;

public class RoutingOptions {
    private List<Double> ranges;
    private Double max_range;
    private Integer range_steps;

    public RoutingOptions() {
    }

    public RoutingOptions(List<Double> ranges) {
        this.ranges = ranges;
    }

    public RoutingOptions(Double max_range, Integer range_steps) {
        this.max_range = max_range;
        this.range_steps = range_steps;
    }

    public RoutingOptions(Double max_range) {
        this.max_range = max_range;
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
