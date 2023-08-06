package org.tud.oas.api.queries.aggregate;

import java.util.ArrayList;
import java.util.List;

import org.tud.oas.routing.ICatchment;
import org.tud.oas.supply.ISupplyView;

public class AggregateQuery {

    public static float[] computeQuery(ISupplyView supply, ICatchment catchment, String computedType) {
        float[] results = new float[supply.pointCount()];
        for (int i = 0; i < supply.pointCount(); i++) {
            int index = i;
            List<Integer> facilities = new ArrayList<>();
            for (int key : catchment.getNeighbours(index)) {
                facilities.add(key);
            }
            if (computedType.equals("min")) {
                if (facilities.isEmpty()) {
                    results[i] = -9999;
                } else {
                    results[i] = (float) facilities.stream().mapToDouble(item -> supply.getSupply(item)).min()
                            .orElse(-9999.0);
                }
            } else if (computedType.equals("max")) {
                if (facilities.isEmpty()) {
                    results[i] = -9999;
                } else {
                    results[i] = (float) facilities.stream().mapToDouble(item -> supply.getSupply(item)).max()
                            .orElse(-9999.0);
                }
            } else if (computedType.equals("median")) {
                if (facilities.isEmpty()) {
                    results[i] = -9999;
                } else {
                    List<Double> temp = new ArrayList<>();
                    for (int item : facilities) {
                        temp.add((double) supply.getSupply(item));
                    }
                    temp.sort(Double::compareTo);
                    if (temp.size() % 2 == 1) {
                        int key = (temp.size() - 1) / 2;
                        results[i] = temp.get(key).floatValue();
                    } else {
                        int key1 = (temp.size() - 2) / 2;
                        int key2 = (temp.size() - 2) / 2 + 1;
                        results[i] = (float) ((temp.get(key1) + temp.get(key2)) / 2);
                    }
                }
            } else if (computedType.equals("mean")) {
                if (facilities.isEmpty()) {
                    results[i] = -9999;
                } else {
                    float sum = 0;
                    for (int key : facilities) {
                        sum += (float) supply.getSupply(key);
                    }
                    results[i] = sum / facilities.size();
                }
            } else if (computedType.equals("sum")) {
                if (facilities.isEmpty()) {
                    results[i] = -9999;
                } else {
                    float sum = 0;
                    for (int key : facilities) {
                        sum += (float) supply.getSupply(key);
                    }
                    results[i] = sum;
                }
            }
        }
        return results;
    }
}
