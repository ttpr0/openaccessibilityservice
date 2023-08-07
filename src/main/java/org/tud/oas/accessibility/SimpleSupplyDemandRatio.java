package org.tud.oas.accessibility;

import java.util.ArrayList;
import java.util.List;

import org.tud.oas.demand.IDemandView;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.ITDMatrix;
import org.tud.oas.routing.RoutingOptions;
import org.tud.oas.supply.ISupplyView;

public class SimpleSupplyDemandRatio {

    /**
     * Computes a simple ratio between reachable supply and demand at every demand
     * location.
     * Formula:
     * $A_i = \frac{\sum{S_j(d_{ij} < d_{max})}{D_i}$
     * $S_j$ denotes the weight of the reachable supply $j$, $D_i$ the demand of the
     * demand point $i$ and $d_{ij}$ the travel-distance between them.
     * 
     * @param demand    Demand locations and weights (results contains ratio for
     *                  every demand point).
     * @param supply    Supply locations and weights.
     * @param max_range Max-Range used in computation $d_{max}$.
     * @param provider  Routing API provider.
     * @return Supply-Demand Ratio for every demand point.
     */
    public static float[] calcAccessibility(IDemandView demand, ISupplyView supply, double max_range,
            IRoutingProvider provider) {
        float[] accessibilities = new float[demand.pointCount()];

        List<Double> ranges = new ArrayList();
        ranges.add(max_range);

        ITDMatrix matrix = provider.requestTDMatrix(demand, supply, "isochrones", new RoutingOptions(ranges));
        try {
            if (matrix == null) {
                return accessibilities;
            }

            for (int f = 0; f < supply.pointCount(); f++) {
                for (int p = 0; p < demand.pointCount(); p++) {
                    float range = matrix.getRange(f, p);
                    if (range < 0 || range > max_range) {
                        continue;
                    }
                    accessibilities[p] += (float) supply.getSupply(f);
                }
            }
            for (int i = 0; i < accessibilities.length; i++) {
                float access = accessibilities[i];
                if (access == 0) {
                    accessibilities[i] = -9999;
                } else {
                    accessibilities[i] = access / demand.getDemand(i);
                }
            }

            return accessibilities;
        } catch (Exception e) {
            e.printStackTrace();
            return new float[0];
        }
    }
}
