package org.tud.oas.accessibility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tud.oas.demand.IDemandView;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.ITDMatrix;
import org.tud.oas.routing.RoutingOptions;
import org.tud.oas.supply.ISupplyView;

public class Simple2SFCA {

    /**
     * Computes the basic two-step-floating-catchment-area accessibility introduced
     * by Luo and Wang (2003).
     * Formula:
     * $A_i = \sum_j{\frac{S_j}{\sum_i{D_i(d_{ij} < d_{max})}}(d_{ij} < d_{max})}$
     * $S_j$ denotes the weight of the reachable supply $j$, $D_i$ the demand of the
     * demand point $i$ and $d_{ij}$ the travel-distance between them.
     * 
     * @param demand   Demand locations and weights ($D_i$).
     * @param supply   Supply locations and weights ($S_j$).
     * @param range    Size of the catchment $d_{max}$.
     * @param provider Routing API provider.
     * @param options  Routing options used (mode of matrix computation and
     *                 ranges of isochrones used)
     * @return two-step-floating-catchment-area value for every demand point.
     */
    public static float[] calc2SFCA(IDemandView demand, ISupplyView supply, double range, IRoutingProvider provider,
            RoutingOptions options) {
        float[] populationWeights = new float[demand.pointCount()];
        float[] facilityWeights = new float[supply.pointCount()];

        Map<Integer, List<Integer>> invertedMapping = new HashMap<>();

        ITDMatrix matrix = provider.requestTDMatrix(demand, supply, options);
        try {
            if (matrix == null) {
                return populationWeights;
            }
            for (int f = 0; f < supply.pointCount(); f++) {
                float weight = 0;
                for (int p = 0; p < demand.pointCount(); p++) {
                    float r = matrix.getRange(f, p);
                    if (r < 0 || r > range) {
                        continue;
                    }
                    int demandCount = demand.getDemand(p);
                    weight += demandCount;

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
            return new float[0];
        }
    }
}
