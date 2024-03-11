package org.tud.oas.routing.gorouting;

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
import org.tud.oas.util.Util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoRoutingProvider implements IRoutingProvider {
    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    private final String url;

    private String profile = "driving-car";
    private String range_type = "time";
    private String location_type = "destination";
    private float isochrone_smoothing = (float) 5.0;
    private Map<String, Object> options;

    public GoRoutingProvider(String url) {
        this.url = url;
    }

    public void setProfile(String profile) {
        if (profile == null) {
            return;
        }
        if (profile.equals("public-transit")) {
            this.profile = "transit-foot";
        } else {
            this.profile = profile;
        }
    }

    public void setRangeType(String range_type) {
        this.range_type = range_type;
    }

    public void setParameter(String name, Object value) {
        switch (name) {
            case "location_type":
                this.location_type = (String) value;
                break;
            case "isochrone_smoothing":
                this.isochrone_smoothing = (float) value;
                break;
            default:
                if (this.options == null) {
                    this.options = new HashMap<String, Object>();
                }
                this.options.put(name, value);
                break;
        }
    }

    public ITDMatrix requestTDMatrix(IDemandView demand, ISupplyView supply, RoutingOptions options) throws Exception {
        Matrix matrix = this.requestTDMatrix(demand, supply, options.getMaxRange());
        if (matrix == null) {
            throw new Exception("failed to request matrix");
        }
        return new TDMatrix(matrix.distances);
    }

    public INNTable requestNearest(IDemandView demand, ISupplyView supply, RoutingOptions options) throws Exception {
        Matrix matrix = this.requestTDMatrix(demand, supply, options.getMaxRange());
        if (matrix == null) {
            throw new Exception("failed to request matrix");
        }
        int[] nearest_table = new int[demand.pointCount()];
        float[] ranges_table = new float[demand.pointCount()];
        for (int j = 0; j < demand.pointCount(); j++) {
            double min_dist = Double.MAX_VALUE;
            int min_index = -1;
            for (int i = 0; i < supply.pointCount(); i++) {
                double dist = matrix.distances[i][j];
                if (dist == -1) {
                    continue;
                }
                if (dist < min_dist) {
                    min_dist = dist;
                    min_index = i;
                }
            }
            nearest_table[j] = min_index;
            ranges_table[j] = (float) min_dist;
        }

        return new NNTable(nearest_table, ranges_table);
    }

    public IKNNTable requestKNearest(IDemandView demand, ISupplyView supply, int n, RoutingOptions options)
            throws Exception {
        Matrix matrix = this.requestTDMatrix(demand, supply, options.getMaxRange());
        if (matrix == null) {
            throw new Exception("failed to request matrix");
        }
        int[][] nearest_table = new int[demand.pointCount()][n];
        float[][] ranges_table = new float[demand.pointCount()][n];
        for (int j = 0; j < demand.pointCount(); j++) {
            int index = j;
            for (int i = 0; i < supply.pointCount(); i++) {
                double range = matrix.distances[i][j];
                int facility_index = i;
                if (range == -1) {
                    continue;
                }
                float last_range = ranges_table[index][n - 1];
                if (last_range > range || last_range == -1) {
                    nearest_table[index][n - 1] = facility_index;
                    ranges_table[index][n - 1] = (float) range;
                    for (int k = n - 2; k >= 0; k--) {
                        float curr_range = ranges_table[index][k];
                        int curr_index = nearest_table[index][k];
                        float prev_range = ranges_table[index][k + 1];
                        int prev_index = nearest_table[index][k + 1];
                        if (curr_range > prev_range || curr_range == -1) {
                            nearest_table[index][k] = prev_index;
                            nearest_table[index][k + 1] = curr_index;
                            ranges_table[index][k] = prev_range;
                            ranges_table[index][k + 1] = curr_range;
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        return new KNNTable(nearest_table, ranges_table);
    }

    public ICatchment requestCatchment(IDemandView demand, ISupplyView supply, double range, RoutingOptions options)
            throws Exception {
        Matrix matrix = this.requestTDMatrix(demand, supply, options.getMaxRange());
        if (matrix == null) {
            throw new Exception("failed to request matrix");
        }
        List<Integer>[] accessibilities = new List[demand.pointCount()];
        for (int i = 0; i < demand.pointCount(); i++) {
            accessibilities[i] = new ArrayList<>();
        }
        for (int i = 0; i < supply.pointCount(); i++) {
            int index = i;
            for (int j = 0; j < demand.pointCount(); j++) {
                double dist = matrix.distances[i][j];
                if (dist != -1 && dist <= range) {
                    accessibilities[j].add(index);
                }
            }
        }

        return new Catchment(accessibilities);
    }

    private Matrix requestTDMatrix(IDemandView demand, ISupplyView supply, double max_range) {
        int point_count = supply.pointCount();
        double[][] sources = new double[point_count][];
        for (int i = 0; i < point_count; i++) {
            int index = i;
            Coordinate p = supply.getCoordinate(index);
            sources[i] = new double[] { p.x, p.y };
        }
        point_count = demand.pointCount();
        double[][] destinations = new double[point_count][];
        for (int i = 0; i < point_count; i++) {
            int index = i;
            Coordinate p = demand.getCoordinate(index);
            destinations[i] = new double[] { p.x, p.y };
        }

        Map<String, Object> request = new HashMap();
        request.put("sources", sources);
        request.put("destinations", destinations);
        request.put("max_range", (Integer) (int) max_range);
        request.put("profile", this.profile);
        request.put("metric", this.range_type);
        request.put("time_window", new int[] { 28800, 36000 });
        request.put("schedule_day", "monday");

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            String req = objectMapper.writeValueAsString(request);

            String response = Util.sendPOST(this.url + "/v1/matrix", req);

            Matrix matrix = objectMapper.readValue(response, Matrix.class);

            return matrix;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
