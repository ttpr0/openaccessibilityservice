package org.tud.oas.accessibility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tud.oas.population.IPopulationView;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.ITDMatrix;

public class Simple2SFCA {

    public static float[] calc2SFCA(IPopulationView population, double[][] facilities, double[] capacities,
            double range, IRoutingProvider provider) {
        float[] populationWeights = new float[population.pointCount()];
        float[] facilityWeights = new float[facilities.length];

        Map<Integer, List<Integer>> invertedMapping = new HashMap<>();

        List<Double> ranges = new ArrayList<>();
        ranges.add(range);

        ITDMatrix matrix = provider.requestTDMatrix(population, facilities, ranges, "isochrones");
        try {
            if (matrix == null) {
                return populationWeights;
            }
            for (int f = 0; f < facilities.length; f++) {
                float weight = 0;
                for (int p = 0; p < population.pointCount(); p++) {
                    float r = matrix.getRange(f, p);
                    if (r == 9999) {
                        continue;
                    }
                    int populationCount = population.getPopulation(p);
                    weight += populationCount;

                    if (!invertedMapping.containsKey(p)) {
                        invertedMapping.put(p, new ArrayList<>(4));
                    }
                    invertedMapping.get(p).add(f);
                }
                if (weight == 0) {
                    facilityWeights[f] = 0;
                } else {
                    facilityWeights[f] = (float) capacities[f] / weight;
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
