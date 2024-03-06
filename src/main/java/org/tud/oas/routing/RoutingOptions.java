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

    public RoutingOptions(String mode, double max_range, int range_steps) {
        this.mode = mode;
        this.max_range = max_range;
        this.range_steps = range_steps;
    }

    public RoutingOptions(String mode, double max_range) {
        this.mode = mode;
        this.max_range = max_range;
    }

    public String getMode() {
        return this.mode;
    }

    public List<Double> getRanges() {
        if (this.ranges != null) {
            return ranges;
        } else if (this.range_steps != null) {
            var ranges = new ArrayList<Double>();
            for (int i = 0; i < this.range_steps; i++) {
                ranges.add(max_range / this.range_steps * (i + 1));
            }
            return ranges;
        } else {
            var ranges = new ArrayList<Double>();
            ranges.add(max_range);
            return ranges;
        }
    }

    public Double getMaxRange() {
        if (this.max_range != null) {
            return this.max_range;
        } else {
            return this.ranges.get(this.ranges.size() - 1);
        }
    }
}
