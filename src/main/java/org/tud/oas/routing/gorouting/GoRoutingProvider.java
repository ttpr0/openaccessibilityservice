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

import java.util.HashMap;
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
        this.profile = profile;
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

    public ITDMatrix requestTDMatrix(IDemandView demand, ISupplyView supply, RoutingOptions options) {
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
        if (options.hasMaxRange()) {
            request.put("max_range", options.getMaxRange());
        } else if (options.hasRanges()) {
            var ranges = options.getRanges();
            request.put("max_range", ranges.get(ranges.size() -1));
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            String req = objectMapper.writeValueAsString(request);

            String response = Util.sendPOST(this.url + "/v1/matrix", req);

            Matrix matrix = objectMapper.readValue(response, Matrix.class);

            return new TDMatrix(matrix.durations);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public INNTable requestNearest(IDemandView demand, ISupplyView supply, RoutingOptions options) {
        return null;
    }

    public IKNNTable requestKNearest(IDemandView demand, ISupplyView supply, int n, RoutingOptions options) {
        return null;
    }

    public ICatchment requestCatchment(IDemandView demand, ISupplyView supply, double range, RoutingOptions options) {
        return null;
    }
}
