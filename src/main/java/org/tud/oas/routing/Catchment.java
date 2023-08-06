package org.tud.oas.routing;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Catchment implements ICatchment {
    public List<Integer>[] sources;

    public Catchment(List<Integer>[] sources) {
        this.sources = sources;
    }

    public Iterable<Integer> getNeighbours(int destination) {
        List<Integer> agg = this.sources[destination];
        if (agg == null) {
            return Collections.emptyList();
        }
        return agg;
    }
}