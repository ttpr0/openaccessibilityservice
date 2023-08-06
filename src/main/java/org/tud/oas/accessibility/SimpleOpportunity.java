package org.tud.oas.accessibility;

import java.util.ArrayList;
import java.util.List;

import org.tud.oas.accessibility.distance_decay.IDistanceDecay;
import org.tud.oas.population.IPopulationView;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.ITDMatrix;

public class SimpleOpportunity {

    public static float[] calcAccessibility(IPopulationView population, double[][] facilities,
            double[] capacities, List<Double> ranges, IDistanceDecay decay, IRoutingProvider provider) {
        float[] accessibilities = new float[population.pointCount()];

        List<Double> rangeList = new ArrayList<>(ranges);

        ITDMatrix matrix = provider.requestTDMatrix(population, facilities, rangeList, "isochrones");
        try {
            if (matrix == null) {
                return accessibilities;
            }

            for (int f = 0; f < facilities.length; f++) {
                for (int p = 0; p < population.pointCount(); p++) {
                    float range = matrix.getRange(f, p);
                    if (range == 9999) {
                        continue;
                    }
                    float rangeFactor = decay.getDistanceWeight(range);

                    accessibilities[p] += (float) capacities[f] * rangeFactor;
                }
            }

            float maxValue = 0;
            for (float accessibility : accessibilities) {
                maxValue = Math.max(maxValue, accessibility);
            }
            for (int i = 0; i < accessibilities.length; i++) {
                float access = accessibilities[i];
                if (access == 0) {
                    accessibilities[i] = -9999;
                } else {
                    accessibilities[i] = access * 100 / maxValue;
                }
            }

            return accessibilities;
        } catch (Exception e) {
            e.printStackTrace();
            return new float[0];
        }
    }
}