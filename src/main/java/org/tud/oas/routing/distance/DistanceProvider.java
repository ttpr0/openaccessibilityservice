package org.tud.oas.routing.distance;

import org.tud.oas.routing.INNTable;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.ITDMatrix;
import org.tud.oas.routing.TDMatrix;
import org.tud.oas.supply.ISupplyView;
import org.tud.oas.routing.NNTable;
import org.tud.oas.routing.RoutingOptions;
import org.tud.oas.routing.KNNTable;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.routing.Catchment;
import org.tud.oas.routing.ICatchment;
import org.tud.oas.routing.IKNNTable;

import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class DistanceProvider implements IRoutingProvider {
    private String metric;
    private String range_type = "time";
    private double avg_speed;
    private Map<String, Object> options;

    public DistanceProvider(String metric, double avg_speed) {
        this.metric = metric;
        this.avg_speed = avg_speed / 3.6;
    }

    public void setProfile(String profile) {
    }

    public void setRangeType(String range_type) {
        this.range_type = range_type;
    }

    public void setParameter(String name, Object value) {
        if (name.equals("avg_speed")) {
            this.avg_speed = (Float) value;
            return;
        }
        if (this.options == null) {
            this.options = new HashMap<String, Object>();
        }
        this.options.put(name, value);
    }

    private double euclideanDistance(Coordinate a, Coordinate b) {
        double d_lon = a.x - b.x;
        double d_lat = a.y - b.y;
        return Math.sqrt(Math.pow(d_lon, 2) + Math.pow(d_lat, 2));
    }

    private double haversineDistance(Coordinate a, Coordinate b) {
        double r = 6365000.0;
        double lat1 = a.y * Math.PI / 180;
        double lat2 = b.y * Math.PI / 180;
        double lon1 = a.x * Math.PI / 180;
        double lon2 = b.x * Math.PI / 180;
        double a_ = Math.pow(Math.sin((lat2 - lat1) / 2), 2);
        double b_ = Math.pow(Math.sin((lon2 - lon1) / 2), 2);
        return 2.0 * r * Math.asin(Math.sqrt(a_ + Math.cos(lat1) * Math.cos(lat2) * b_));
    }

    public ITDMatrix requestTDMatrix(IDemandView demand, ISupplyView supply, RoutingOptions options) {
        int sup_count = supply.pointCount();
        int dem_count = demand.pointCount();
        double[][] distances = new double[sup_count][dem_count];

        BiFunction<Coordinate, Coordinate, Double> func;
        if (this.metric.equals("eucledian")) {
            func = (a, b) -> {
                return euclideanDistance(a, b);
            };
        } else if (this.metric.equals("spherical")) {
            func = (a, b) -> {
                return haversineDistance(a, b);
            };
        } else {
            func = (a, b) -> {
                return -1.0;
            };
        }

        for (int i = 0; i < sup_count; i++) {
            Coordinate s = supply.getCoordinate(i);
            for (int j = 0; j < dem_count; j++) {
                Coordinate d = demand.getCoordinate(j);
                double dist = func.apply(s, d);
                if (this.range_type.equals("time")) {
                    dist = dist / this.avg_speed;
                }
                // if (dist > options.getMaxRange()) {
                // dist = -1;
                // }
                distances[i][j] = dist;
            }
        }

        return new TDMatrix(distances);
    }

    public INNTable requestNearest(IDemandView demand, ISupplyView supply, RoutingOptions options) {
        int sup_count = supply.pointCount();
        int dem_count = demand.pointCount();
        int[] nearest_table = new int[dem_count];
        float[] ranges_table = new float[dem_count];
        for (int j = 0; j < dem_count; j++) {
            nearest_table[j] = -1;
            ranges_table[j] = -1;
        }

        BiFunction<Coordinate, Coordinate, Double> func;
        if (this.metric.equals("eucledian")) {
            func = (a, b) -> {
                return euclideanDistance(a, b);
            };
        } else if (this.metric.equals("spherical")) {
            func = (a, b) -> {
                return haversineDistance(a, b);
            };
        } else {
            func = (a, b) -> {
                return -1.0;
            };
        }

        for (int i = 0; i < dem_count; i++) {
            for (int j = 0; j < sup_count; j++) {
                Coordinate d = demand.getCoordinate(i);
                Coordinate s = supply.getCoordinate(j);

                double dist = func.apply(s, d);
                if (this.range_type.equals("time")) {
                    dist = dist / this.avg_speed;
                }

                if (ranges_table[i] == -1 || dist < ranges_table[i]) {
                    nearest_table[i] = j;
                    ranges_table[i] = (float) dist;
                }
            }
        }

        return new NNTable(nearest_table, ranges_table);
    }

    public IKNNTable requestKNearest(IDemandView demand, ISupplyView supply, int n, RoutingOptions options) {
        int sup_count = supply.pointCount();
        int dem_count = demand.pointCount();
        int[][] nearest_table = new int[dem_count][n];
        float[][] ranges_table = new float[dem_count][n];
        for (int j = 0; j < dem_count; j++) {
            for (int i = 0; i < n; i++) {
                nearest_table[j][i] = -1;
                ranges_table[j][i] = -1;
            }
        }

        BiFunction<Coordinate, Coordinate, Double> func;
        if (this.metric.equals("eucledian")) {
            func = (a, b) -> {
                return euclideanDistance(a, b);
            };
        } else if (this.metric.equals("spherical")) {
            func = (a, b) -> {
                return haversineDistance(a, b);
            };
        } else {
            func = (a, b) -> {
                return -1.0;
            };
        }

        for (int i = 0; i < dem_count; i++) {
            for (int j = 0; j < sup_count; j++) {
                Coordinate d = demand.getCoordinate(i);
                Coordinate s = supply.getCoordinate(j);

                double dist = func.apply(s, d);
                if (this.range_type.equals("time")) {
                    dist = dist / this.avg_speed;
                }

                // insert new range while keeping array-dimension sorted
                int index = i;
                int facility_index = j;
                float last_range = ranges_table[index][n - 1];
                if (last_range > dist || last_range == -1) {
                    nearest_table[index][n - 1] = facility_index;
                    ranges_table[index][n - 1] = (float) dist;
                    for (int k = n - 2; k >= 0; k--) {
                        float curr_range = ranges_table[index][k];
                        float prev_range = ranges_table[index][k + 1];
                        if (curr_range > prev_range || curr_range == -1) {
                            nearest_table[index][k] = nearest_table[index][k + 1];
                            ranges_table[index][k] = prev_range;
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        return new KNNTable(nearest_table, ranges_table);
    }

    public ICatchment requestCatchment(IDemandView demand, ISupplyView supply, double range, RoutingOptions options) {
        int sup_count = supply.pointCount();
        int dem_count = demand.pointCount();
        List<Integer>[] accessibilities = new List[dem_count];

        BiFunction<Coordinate, Coordinate, Double> func;
        if (this.metric.equals("eucledian")) {
            func = (a, b) -> {
                return euclideanDistance(a, b);
            };
        } else if (this.metric.equals("spherical")) {
            func = (a, b) -> {
                return haversineDistance(a, b);
            };
        } else {
            func = (a, b) -> {
                return -1.0;
            };
        }

        for (int i = 0; i < dem_count; i++) {
            for (int j = 0; j < sup_count; j++) {
                Coordinate d = demand.getCoordinate(i);
                Coordinate s = supply.getCoordinate(j);

                double dist = func.apply(s, d);
                if (this.range_type.equals("time")) {
                    dist = dist / this.avg_speed;
                }

                if (accessibilities[i] == null) {
                    List<Integer> access = new ArrayList();
                    accessibilities[i] = access;
                }

                if (dist <= range) {
                    accessibilities[i].add(j);
                }
            }
        }

        return new Catchment(accessibilities);
    }
}
