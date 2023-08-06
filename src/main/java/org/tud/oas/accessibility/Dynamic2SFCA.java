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

public class Dynamic2SFCA {

    public static float[] calc2SFCA(IDemandView demand, int[] rangeIndices, ISupplyView supply, List<Double> ranges,
            IRoutingProvider provider) {
        float[] populationWeights = new float[demand.pointCount()];
        float[] facilityWeights = new float[supply.pointCount()];

        Map<Integer, List<Integer>> invertedMapping = new HashMap<>();

        ITDMatrix matrix = provider.requestTDMatrix(demand, supply, "isochrones", new RoutingOptions(ranges));
        try {
            if (matrix == null) {
                return populationWeights;
            }
            for (int f = 0; f < supply.pointCount(); f++) {
                float weight = 0;
                for (int p = 0; p < demand.pointCount(); p++) {
                    float range = matrix.getRange(f, p);
                    if (range == 9999) {
                        continue;
                    }
                    int rangeIndex = ranges.indexOf(range);
                    if (rangeIndex != rangeIndices[p]) {
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
            return new float[0];
        }
    }
}
