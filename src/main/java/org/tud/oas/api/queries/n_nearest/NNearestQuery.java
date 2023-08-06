package org.tud.oas.api.queries.n_nearest;

import java.util.ArrayList;
import java.util.List;

import org.tud.oas.routing.IKNNTable;
import org.tud.oas.supply.ISupplyView;

class NNearestQuery {

    public static float[] computeQuery(ISupplyView supply, IKNNTable table, String computedType, int count) {
        float[] results = new float[supply.pointCount()];
        for (int i = 0; i < supply.pointCount(); i++) {
            int index = i;
            List<Double> vals = new ArrayList<>();
            for (int item = 0; item < count; item++) {
                int key = table.getKNearest(index, item);
                if (key == -1) {
                    vals.add(-9999.0);
                } else {
                    vals.add((double) supply.getSupply(key));
                }
            }
            vals.removeIf(item -> item == -9999.0);

            if (computedType.equals("min")) {
                if (vals.isEmpty()) {
                    results[i] = -9999;
                } else {
                    results[i] = (float) vals.stream().mapToDouble(Double::doubleValue).min().orElse(-9999.0);
                }
            } else if (computedType.equals("max")) {
                if (vals.size() < count) {
                    results[i] = -9999;
                } else {
                    results[i] = (float) vals.stream().mapToDouble(Double::doubleValue).max().orElse(-9999.0);
                }
            } else if (computedType.equals("median")) {
                if (vals.size() < count) {
                    results[i] = -9999;
                } else {
                    vals.sort(Double::compareTo);
                    if (vals.size() % 2 == 1) {
                        int key = (vals.size() - 1) / 2;
                        results[i] = vals.get(key).floatValue();
                    } else {
                        int key1 = (vals.size() - 2) / 2;
                        int key2 = (vals.size() - 2) / 2 + 1;
                        results[i] = (float) ((vals.get(key1) + vals.get(key2)) / 2);
                    }
                }
            } else if (computedType.equals("mean")) {
                if (vals.size() < count) {
                    results[i] = -9999;
                } else {
                    results[i] = (float) (vals.stream().mapToDouble(Double::doubleValue).sum() / vals.size());
                }
            } else if (computedType.equals("sum")) {
                if (vals.size() < count) {
                    results[i] = -9999;
                } else {
                    results[i] = (float) vals.stream().mapToDouble(Double::doubleValue).sum();
                }
            }
        }
        return results;
    }
}
