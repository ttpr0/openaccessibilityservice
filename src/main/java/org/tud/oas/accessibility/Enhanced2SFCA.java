package org.tud.oas.accessibility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tud.oas.accessibility.distance_decay.IDistanceDecay;
import org.tud.oas.population.IPopulationView;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.ITDMatrix;

public class Enhanced2SFCA {

    public static float[] calc2SFCA(IPopulationView population, double[][] facilities, double[] capacities,
            List<Double> ranges, IDistanceDecay decay, IRoutingProvider provider, String mode) {
        float[] populationWeights = new float[population.pointCount()];
        float[] facilityWeights = new float[facilities.length];

        Map<Integer, List<FacilityReference>> invertedMapping = new HashMap<>();

        ITDMatrix matrix = provider.requestTDMatrix(population, facilities, ranges, mode);
        try {
            if (matrix == null) {
                return populationWeights;
            }
            for (int f = 0; f < facilities.length; f++) {
                float weight = 0;
                for (int p = 0; p < population.pointCount(); p++) {
                    float range = matrix.getRange(f, p);
                    if (range == 9999) {
                        continue;
                    }
                    float rangeFactor = decay.getDistanceWeight(range);
                    int populationCount = population.getPopulation(p);
                    weight += populationCount * rangeFactor;

                    if (!invertedMapping.containsKey(p)) {
                        invertedMapping.put(p, new ArrayList<>(4));
                    }
                    invertedMapping.get(p).add(new FacilityReference(f, range));
                }
                if (weight == 0) {
                    facilityWeights[f] = 0;
                } else {
                    facilityWeights[f] = (float) capacities[f] / weight;
                }
            }

            for (int index : invertedMapping.keySet()) {
                List<FacilityReference> refs = invertedMapping.get(index);
                if (refs == null) {
                    continue;
                } else {
                    float weight = 0;
                    for (FacilityReference fref : refs) {
                        double rangeFactor = decay.getDistanceWeight(fref.range);
                        weight += facilityWeights[fref.index] * rangeFactor;
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

class FacilityReference {
    public int index;
    public float range;

    public FacilityReference(int index, float range) {
        this.index = index;
        this.range = range;
    }
}
