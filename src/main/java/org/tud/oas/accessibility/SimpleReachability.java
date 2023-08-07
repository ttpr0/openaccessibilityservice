package org.tud.oas.accessibility;

import java.util.List;

import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.RoutingOptions;
import org.tud.oas.supply.ISupplyView;
import org.tud.oas.accessibility.distance_decay.IDistanceDecay;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.routing.INNTable;

public class SimpleReachability {

    /**
     * Computes a simple reachability measure. For every demand point the closest
     * reachable supply (multiplied by distance decay) is computed.
     * Example formula for linear decay:
     * $A_i = S_j * (1-\frac{d_{ij}}{d_{max}})$
     * $S_j$ denotes the closest supply point $j$ to the demand point $i$.
     * $d_{ij}$ the distance between them.
     * 
     * @param demand   Demand locations.
     * @param supply   Supply locations and weights ($S_j$).
     * @param ranges   Ranges of isochrones used in computation of distances
     *                 $d_{ij}$.
     * @param decay    Distance decay.
     * @param provider Routing API provider.
     * @return Reachability for every demand point.
     */
    public float[] calcAccessibility(IDemandView demand, ISupplyView supply, List<Double> ranges, IDistanceDecay decay,
            IRoutingProvider provider) {
        float[] accessibilities = new float[demand.pointCount()];
        double max_range = ranges.get(ranges.size() - 1);

        INNTable table = provider.requestNearest(demand, supply, "isochrones", new RoutingOptions(ranges));
        if (table == null) {
            return accessibilities;
        }

        for (int p = 0; p < demand.pointCount(); p++) {
            float range = table.getNearestRange(p);
            if (range < 0 || range > max_range) {
                continue;
            }
            int f = table.getNearest(p);
            float rangeFactor = decay.getDistanceWeight(range);

            accessibilities[p] = (float) supply.getSupply(f) * rangeFactor;
        }

        for (int i = 0; i < accessibilities.length; i++) {
            float access = accessibilities[i];
            if (access == 0) {
                accessibilities[i] = -9999;
            } else {
                accessibilities[i] = access;
            }
        }

        return accessibilities;
    }
}
