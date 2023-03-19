package org.tud.oas.routing;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public interface IRoutingProvider {
    List<IsochroneCollection> requestIsochrones(Double[][] locations, List<Double> ranges);

    BlockingQueue<IsochroneCollection> requestIsochronesStream(Double[][] locations, List<Double> ranges);

    List<IsoRaster> requestIsoRasters(Double[][] locations, double max_range);

    BlockingQueue<IsoRaster> requestIsoRasterStream(Double[][] locations, double max_range);
}
