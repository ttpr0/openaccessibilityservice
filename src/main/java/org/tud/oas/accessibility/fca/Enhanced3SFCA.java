package org.tud.oas.accessibility.fca;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tud.oas.accessibility.distance_decay.IDistanceDecay;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.ITDMatrix;
import org.tud.oas.routing.RoutingOptions;
import org.tud.oas.supply.ISupplyView;

public class Enhanced3SFCA {

    /**
     * Computes the enhanced three-step-floating-catchment-area accessibility
     * introduced by Wan et al. (2012).
     * Formula:
     * $A_i = \sum_j{\frac{S_j}{\sum_i{D_i * w_{ij} * G_{ij}}} * w_{ij} * G_{ij}}$
     * $S_j$ denotes the weight of the reachable supply $j$, $D_i$ the demand of the
     * demand point $i$ and $w_{ij} ~ d_{ij}$ the travel-friction (distance decay)
     * between them and $G_{ij} the propability of demand $D_i$ choosing supply
     * $S_j$ (Propabilities are computed using the Huff Model, see Luo 2014).
     * 
     * @param demand     Demand locations and weights ($D_i$).
     * @param supply     Supply locations and weights ($S_j$).
     * @param attraction Supply weights used in propability computation (all ones
     *                   will result in original 3SFCA method).
     * @param decay      Distance decay.
     * @param provider   Routing API provider.
     * @param options    Computation mode ("isochrones", "matrix") and Ranges of
     *                   isochrones used in computation of distances $d_{ij}$.
     * @return three-step-floating-catchment-area value for every demand point.
     */
    public static float[] calc2SFCA(IDemandView demand, ISupplyView supply, float[] attraction, IDistanceDecay decay,
            IRoutingProvider provider, RoutingOptions options) {
        float[] populationWeights = new float[demand.pointCount()];
        float[] facilityWeights = new float[supply.pointCount()];

        Map<Integer, List<FacilityReference>> invertedMapping = new HashMap<>();

        ITDMatrix matrix = provider.requestTDMatrix(demand, supply, options);
        try {
            if (matrix == null) {
                return populationWeights;
            }

            // compute propabilities
            float[][] selection_weights = new float[demand.pointCount()][supply.pointCount()];
            for (int p = 0; p < demand.pointCount(); p++) {
                float sum = 0;
                for (int f = 0; f < supply.pointCount(); f++) {
                    float range = matrix.getRange(f, p);
                    sum += attraction[f] * decay.getDistanceWeight(range);
                }
                for (int f = 0; f < supply.pointCount(); f++) {
                    float range = matrix.getRange(f, p);
                    float val = attraction[f] * decay.getDistanceWeight(range);
                    selection_weights[p][f] = val / sum;
                }
            }

            // compute supply ratios
            for (int f = 0; f < supply.pointCount(); f++) {
                float weight = 0;
                for (int p = 0; p < demand.pointCount(); p++) {
                    float range = matrix.getRange(f, p);
                    if (range < 0) {
                        continue;
                    }
                    float rangeFactor = decay.getDistanceWeight(range);
                    int populationCount = demand.getDemand(p);
                    float propability = selection_weights[p][f];
                    weight += populationCount * rangeFactor * propability;

                    if (!invertedMapping.containsKey(p)) {
                        invertedMapping.put(p, new ArrayList<>(4));
                    }
                    invertedMapping.get(p).add(new FacilityReference(f, range));
                }
                if (weight == 0) {
                    facilityWeights[f] = 0;
                } else {
                    facilityWeights[f] = (float) supply.getSupply(f) / weight;
                }
            }

            // sum up supply ratios on demand points
            for (int index : invertedMapping.keySet()) {
                List<FacilityReference> refs = invertedMapping.get(index);
                if (refs == null) {
                    continue;
                } else {
                    float weight = 0;
                    for (FacilityReference fref : refs) {
                        double rangeFactor = decay.getDistanceWeight(fref.range);
                        float propability = selection_weights[index][fref.index];
                        weight += facilityWeights[fref.index] * rangeFactor * propability;
                    }
                    populationWeights[index] = weight;
                }
            }

            return populationWeights;
        } catch (Exception e) {
            e.printStackTrace();
            return new float[0];
        }
    }
}
