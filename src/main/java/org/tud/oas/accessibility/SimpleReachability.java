package org.tud.oas.accessibility;

import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.ITDMatrix;
import org.tud.oas.routing.RoutingOptions;
import org.tud.oas.supply.ISupplyView;
import org.tud.oas.accessibility.distance_decay.IDistanceDecay;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.routing.INNTable;

public class SimpleReachability {

    /**
     * Computes a simple reachability measure. For every demand point the closest
     * reachable supply (represented by distance decay) is computed.
     * Example formula for linear decay:
     * $A_i = 1-\frac{d_{ij}}{d_{max}}$
     * $j$ denotes the closest supply point to the demand point $i$.
     * $d_{ij}$ the distance between them.
     * 
     * @param demand   Demand locations.
     * @param supply   Supply locations.
     * @param decay    Distance decay.
     * @param provider Routing API provider.
     * @param options  Mode and anges of isochrones used in computation of distances
     *                 $d_{ij}$.
     * @return Reachability for every demand point.
     */
    public static float[] calcAccessibility(IDemandView demand, ISupplyView supply, IDistanceDecay decay,
            IRoutingProvider provider, RoutingOptions options) throws Exception {
        float[] accessibilities = new float[demand.pointCount()];
        double max_range = decay.getMaxDistance();

        INNTable table;
        try {
            table = provider.requestNearest(demand, supply, options);
        } catch (Exception e) {
            throw new Exception("failed to compute k-nearest-neighbours:" + e.getMessage());
        }

        try {
            for (int p = 0; p < demand.pointCount(); p++) {
                float range = table.getNearestRange(p);
                if (range < 0 || range > max_range) {
                    continue;
                }
                float rangeFactor = decay.getDistanceWeight(range);

                accessibilities[p] = rangeFactor;
            }

            return accessibilities;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("failed to compute accessibility");
        }
    }
}
