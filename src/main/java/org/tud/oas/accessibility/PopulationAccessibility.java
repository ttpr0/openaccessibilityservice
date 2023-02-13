package org.tud.oas.accessibility;

import java.util.ArrayList;
import java.util.List;

public class PopulationAccessibility {
    public List<Integer> ranges;

    PopulationAccessibility() {
        this.ranges = new ArrayList<Integer>();
    }

    void addRange(int range) {
        this.ranges.add(range);
    }
}
