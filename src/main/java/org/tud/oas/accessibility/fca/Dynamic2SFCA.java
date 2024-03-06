package org.tud.oas.accessibility.fca;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tud.oas.demand.IDemandView;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.ITDMatrix;
import org.tud.oas.routing.RoutingOptions;
import org.tud.oas.supply.ISupplyView;

public class Dynamic2SFCA {

    /**
     * Computes a dynamic two-step-floating-catchment-area accessibility as
     * introduced by McGrail and Humphreys (2014).
     * Formula:
     * $A_i = \sum_j{\frac{S_j}{\sum_i{D_i(d_{ij} < d_{max, i})}}(d_{ij} < d_{max,
     * i})}$
     * $S_j$ denotes the weight of the reachable supply $j$, $D_i$ the demand of the
     * demand point $i$ and $d_{ij}$ the travel-distance between them. Compared to
     * the basic 2SFCA method the catchment size and therefore $d_{max}$ is
     * dependant on the demand point $i$.
     * 
     * @param demand       Demand locations and weights ($D_i$).
     * @param supply       Supply locations and weights ($S_j$).
     * @param ranges       Catchment-sizes $d_{max}$.
     * @param rangeIndices Index of the catchment-size (from ranges) for every
     *                     demand point
     * @param provider     Routing API provider.
     * @param options      Routing options used (mode of matrix computation and
     *                     ranges of isochrones used)
     * @return two-step-floating-catchment-area value for every demand point.
     */
    public static float[] calc2SFCA(IDemandView demand, ISupplyView supply, List<Double> ranges, int[] rangeIndices,
            IRoutingProvider provider, RoutingOptions options) throws Exception {
        float[] populationWeights = new float[demand.pointCount()];
        float[] facilityWeights = new float[supply.pointCount()];

        Map<Integer, List<Integer>> invertedMapping = new HashMap<>();

        ITDMatrix matrix;
        try {
            matrix = provider.requestTDMatrix(demand, supply, options);
        } catch (Exception e) {
            throw new Exception("failed to compute travel-time-matrix:" + e.getMessage());
        }

        try {
            for (int f = 0; f < supply.pointCount(); f++) {
                float weight = 0;
                for (int p = 0; p < demand.pointCount(); p++) {
                    float range = matrix.getRange(f, p);
                    if (range == 9999) {
                        continue;
                    }
                    if (range > ranges.get(rangeIndices[p])) {
                        continue;
                    }
                    int populationCount = demand.getDemand(p);
                    weight += populationCount;

                    if (!invertedMapping.containsKey(p)) {
                        invertedMapping.put(p, new ArrayList<>(4));
                    }
                    invertedMapping.get(p).add(f);
                }
                if (weight == 0) {
                    facilityWeights[f] = 0;
                } else {
                    facilityWeights[f] = (float) supply.getSupply(f) / weight;
                }
            }

            for (int index : invertedMapping.keySet()) {
                List<Integer> refs = invertedMapping.get(index);
                if (refs == null) {
                    continue;
                } else {
                    float weight = 0;
                    for (int fref : refs) {
                        weight += facilityWeights[fref];
                    }
                    populationWeights[index] = weight;
                }
            }

            return populationWeights;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Failed to compute accessibility");
        }
    }
}
