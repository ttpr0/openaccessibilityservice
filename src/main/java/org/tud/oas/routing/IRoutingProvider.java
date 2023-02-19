package org.tud.oas.routing;

import java.util.List;

public interface IRoutingProvider {
    List<IsochroneCollection> requestIsochrones(Double[][] locations, List<Double> ranges);
}
