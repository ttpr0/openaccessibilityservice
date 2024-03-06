package org.tud.oas.accessibility;

import org.tud.oas.accessibility.distance_decay.IDistanceDecay;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.ITDMatrix;
import org.tud.oas.routing.RoutingOptions;
import org.tud.oas.supply.ISupplyView;

public class SimpleOpportunity {

    /**
     * Computes a simple opportunity measure. For every demand point the product of
     * reachable supply and distance decay are summed up.
     * Example formula for linear decay:
     * $A_i = \sum{S_j * (1-\frac{d_{ij}}{d_{max}})}$
     * 
     * When using the Gravity-Decay function ($d_{ij}^{-\beta}$) this method can
     * also compute the basic gravity accessibility introduced by Hansen (1959).
     * Formula:
     * $A_i = \sum{\frac{S_j}{d_{ij}^{\beta}}}$
     * 
     * @param demand   Demand locations (results contains accumulated opportunity
     *                 for every demand point).
     * @param supply   Supply locations and weights ($S_j$).
     * @param decay    Distance decay.
     * @param provider Routing API provider.
     * @param options  Mode and ranges of isochrones used in computation of
     *                 distances $d_{ij}$.
     * @return Accumulated Opportunity for every demand point.
     */
    public static float[] calcAccessibility(IDemandView demand, ISupplyView supply, IDistanceDecay decay,
            IRoutingProvider provider, RoutingOptions options) throws Exception {
        float[] accessibilities = new float[demand.pointCount()];

        ITDMatrix matrix;
        try {
            matrix = provider.requestTDMatrix(demand, supply, options);
        } catch (Exception e) {
            throw new Exception("failed to compute travel-time-matrix:" + e.getMessage());
        }

        try {
            for (int f = 0; f < supply.pointCount(); f++) {
                for (int p = 0; p < demand.pointCount(); p++) {
                    float range = matrix.getRange(f, p);
                    if (range < 0) {
                        continue;
                    }
                    float rangeFactor = decay.getDistanceWeight(range);

                    accessibilities[p] += (float) supply.getSupply(f) * rangeFactor;
                }
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
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("failed to compute accessibility.");
        }
    }
}