package org.tud.oas.accessibility.fca;

import org.tud.oas.accessibility.distance_decay.IDistanceDecay;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.routing.IKNNTable;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.RoutingOptions;
import org.tud.oas.supply.ISupplyView;

public class NNM2SFCA {

    /**
     * Computes the nearest-neighbour modified two-step-floating-catchment-area
     * accessibility introduced by Jamtsho et al. 2015.
     * Formula:
     * $A_i = \sum_j{\frac{S_j}{\sum_i{D_i * w_{ij}}} * w_{ij}^2}$
     * $S_j$ denotes the weight of the reachable supply $j$, $D_i$ the demand of the
     * demand point $i$ and $w_{ij} ~ d_{ij}$ the travel-friction (distance decay)
     * between them. Compared to the modified 2sfca method at max n nearest
     * neighbours are considered.
     * 
     * @param demand   Demand locations and weights ($D_i$).
     * @param supply   Supply locations and weights ($S_j$).
     * @param n        number of nearest neighbours
     * @param decay    Distance decay.
     * @param provider Routing API provider.
     * @param options  Computation mode ("isochrones", "matrix") and Ranges of
     *                 isochrones used in computation of distances $d_{ij}$.
     * @return two-step-floating-catchment-area value for every demand point.
     */
    public static float[] calc2SFCA(IDemandView demand, ISupplyView supply, int n, IDistanceDecay decay,
            IRoutingProvider provider, RoutingOptions options) {
        float[] populationWeights = new float[demand.pointCount()];
        float[] facilityWeights = new float[supply.pointCount()];

        IKNNTable table = provider.requestKNearest(demand, supply, n, options);
        try {
            if (table == null) {
                return populationWeights;
            }

            // accumulate demand
            for (int p = 0; p < demand.pointCount(); p++) {
                for (int k = 0; k < n; k++) {
                    int f = table.getKNearest(p, k);
                    float range = table.getKNearestRange(p, k);
                    if (range < 0) {
                        continue;
                    }
                    float rangeFactor = decay.getDistanceWeight(range);
                    int populationCount = demand.getDemand(p);
                    facilityWeights[f] += populationCount * rangeFactor;
                }
            }

            // compute supply demand ratio at supply locations
            for (int f = 0; f < supply.pointCount(); f++) {
                float weight = facilityWeights[f];
                if (weight == 0) {
                    facilityWeights[f] = 0;
                } else {
                    facilityWeights[f] = (float) supply.getSupply(f) / weight;
                }
            }

            // sum up ratios at demand locations
            for (int p = 0; p < demand.pointCount(); p++) {
                float weight = 0;
                for (int k = 0; k < n; k++) {
                    int f = table.getKNearest(p, k);
                    float range = table.getKNearestRange(p, k);
                    if (range < 0) {
                        continue;
                    }
                    double rangeFactor = decay.getDistanceWeight(range);
                    weight += facilityWeights[f] * rangeFactor * rangeFactor;
                }
                populationWeights[p] = weight;
            }

            return populationWeights;
        } catch (Exception e) {
            e.printStackTrace();
            return new float[0];
        }
    }
}
