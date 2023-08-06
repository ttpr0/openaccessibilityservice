package org.tud.oas.accessibility;

import java.util.ArrayList;
import java.util.List;

import org.tud.oas.accessibility.distance_decay.IDistanceDecay;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.ITDMatrix;
import org.tud.oas.routing.RoutingOptions;
import org.tud.oas.supply.ISupplyView;

public class SimpleOpportunity {

    public static float[] calcAccessibility(IDemandView demand, ISupplyView supply, List<Double> ranges,
            IDistanceDecay decay, IRoutingProvider provider) {
        float[] accessibilities = new float[demand.pointCount()];

        ITDMatrix matrix = provider.requestTDMatrix(demand, supply, "isochrones", new RoutingOptions(ranges));
        try {
            if (matrix == null) {
                return accessibilities;
            }

            for (int f = 0; f < supply.pointCount(); f++) {
                for (int p = 0; p < demand.pointCount(); p++) {
                    float range = matrix.getRange(f, p);
                    if (range == 9999) {
                        continue;
                    }
                    float rangeFactor = decay.getDistanceWeight(range);

                    accessibilities[p] += (float) supply.getSupply(f) * rangeFactor;
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