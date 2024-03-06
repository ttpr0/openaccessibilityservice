package org.tud.oas.routing.ors;

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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.algorithm.locate.SimplePointInAreaLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.simplify.PolygonHullSimplifier;
import org.locationtech.jts.geom.Envelope;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;

public class ORSProvider implements IRoutingProvider {
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    // private static ExecutorService executor =
    // Executors.newVirtualThreadPerTaskExecutor();

    private final String url;

    private String profile = "driving-car";
    private String range_type = "time";
    private String location_type = "destination";
    private float isochrone_smoothing = (float) 5.0;
    private Map<String, Object> options;

    public ORSProvider(String url) {
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

    public ITDMatrix requestTDMatrix(IDemandView demand, ISupplyView supply, RoutingOptions options) throws Exception {
        switch (options.getMode()) {
            case "isochrones":
                return this.requestMatrixIsochrones(demand, supply, options.getRanges());
            case "matrix":
                return this.requestMatrixMatrix(demand, supply, options.getMaxRange());
            case "isoraster":
                return this.requestMatrixIsoraster(demand, supply, options.getMaxRange());
            default:
                return this.requestMatrixMatrix(demand, supply, options.getMaxRange());
        }
    }

    private ITDMatrix requestMatrixIsochrones(IDemandView demand, ISupplyView supply, List<Double> ranges)
            throws Exception {
        double[][] matrix = new double[supply.pointCount()][demand.pointCount()];
        for (int i = 0; i < supply.pointCount(); i++) {
            for (int j = 0; j < demand.pointCount(); j++) {
                matrix[i][j] = -1;
            }
        }

        int point_count = supply.pointCount();
        double[][] facilities = new double[point_count][];
        for (int i = 0; i < point_count; i++) {
            Coordinate p = supply.getCoordinate(i);
            facilities[i] = new double[] { p.x, p.y };
        }

        BlockingQueue<IsochroneCollection> collection = this.requestIsochronesStream(facilities, ranges);
        boolean has_failed = false;
        String error = "";
        for (int f = 0; f < supply.pointCount(); f++) {
            IsochroneCollection isochrones;
            try {
                isochrones = collection.take();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            if (has_failed) {
                continue;
            }
            if (isochrones.isNull()) {
                has_failed = true;
                error = isochrones.getError();
                continue;
            }

            try {
                int facility_index = isochrones.getID();

                Set<Integer> visited = new HashSet<Integer>(10000);
                for (int i = 0; i < isochrones.getIsochronesCount(); i++) {
                    Isochrone isochrone = isochrones.getIsochrone(i);
                    double range = isochrone.getValue();

                    Geometry iso = isochrone.getGeometry();
                    Envelope env = iso.getEnvelopeInternal();

                    Iterable<Integer> points = demand.getPointsInEnvelop(env);

                    for (int index : points) {
                        if (visited.contains(index)) {
                            continue;
                        }
                        Coordinate p = demand.getCoordinate(index);
                        int location = SimplePointInAreaLocator.locate(p, iso);
                        if (location == Location.INTERIOR) {
                            matrix[facility_index][index] = (float) range;
                            visited.add(index);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                has_failed = true;
                error = "Unexpected error during matrix computation";
            }
        }

        if (has_failed) {
            throw new Exception(error);
        }
        return new TDMatrix(matrix);
    }

    private ITDMatrix requestMatrixMatrix(IDemandView demand, ISupplyView supply, double max_range) throws Exception {
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
        Matrix matrix = this.requestMatrix(sources, destinations);
        if (matrix.isNull()) {
            throw new Exception(matrix.getError());
        }

        if (this.range_type.equals("time")) {
            if (!matrix.hasDurations()) {
                throw new Exception("durations are null");
            }
            return new TDMatrix(matrix.getDurations());
        } else {
            if (!matrix.hasDistances()) {
                throw new Exception("distances are null");
            }
            return new TDMatrix(matrix.getDistances());
        }
    }

    private ITDMatrix requestMatrixIsoraster(IDemandView demand, ISupplyView supply, double max_range)
            throws Exception {
        double[][] matrix = new double[supply.pointCount()][demand.pointCount()];
        for (int i = 0; i < supply.pointCount(); i++) {
            for (int j = 0; j < demand.pointCount(); j++) {
                matrix[i][j] = -1;
            }
        }

        int point_count = supply.pointCount();
        double[][] facilities = new double[point_count][];
        for (int i = 0; i < point_count; i++) {
            Coordinate p = supply.getCoordinate(i);
            facilities[i] = new double[] { p.x, p.y };
        }

        IsoRaster isoraster = this.requestIsoRaster(facilities, max_range);
        if (isoraster.isNull()) {
            throw new Exception(isoraster.getError());
        }

        double[][] extend = isoraster.getEnvelope();
        Envelope env = new Envelope(extend[0][0], extend[3][0], extend[2][1], extend[1][1]);
        Iterable<Integer> points = demand.getPointsInEnvelop(env);

        for (int index : points) {
            Coordinate p = demand.getCoordinate(index);
            IsoRasterAccessor accessor = isoraster.getAccessor(p);
            if (accessor != null) {

                for (int f : accessor.getFacilities()) {
                    float range = accessor.getRange(f);

                    matrix[f][index] = range;
                }
            }
        }

        return new TDMatrix(matrix);
    }

    public INNTable requestNearest(IDemandView demand, ISupplyView supply, RoutingOptions options) throws Exception {
        int[] nearest_table = new int[demand.pointCount()];
        float[] ranges_table = new float[demand.pointCount()];
        for (int j = 0; j < demand.pointCount(); j++) {
            nearest_table[j] = -1;
            ranges_table[j] = -1;
        }

        List<Double> ranges = options.getRanges();

        int point_count = supply.pointCount();
        double[][] facilities = new double[point_count][];
        for (int i = 0; i < point_count; i++) {
            Coordinate p = supply.getCoordinate(i);
            facilities[i] = new double[] { p.x, p.y };
        }

        Map<Double, Geometry> polygons = new HashMap<Double, Geometry>(ranges.size());
        BlockingQueue<IsochroneCollection> collection = this.requestIsochronesStream(facilities, ranges);
        boolean has_failed = false;
        String error = "";
        for (int j = 0; j < supply.pointCount(); j++) {
            IsochroneCollection isochrones;
            try {
                isochrones = collection.take();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            if (has_failed) {
                continue;
            }
            if (isochrones.isNull()) {
                has_failed = true;
                error = isochrones.getError();
                continue;
            }

            try {
                for (int i = 0; i < isochrones.getIsochronesCount(); i++) {
                    Isochrone isochrone = isochrones.getIsochrone(i);
                    double range = isochrone.getValue();

                    if (!polygons.containsKey(range)) {
                        polygons.put(range, isochrone.getGeometry());
                    } else {
                        Geometry geometry = polygons.get(range);
                        Geometry union = geometry.union(isochrone.getGeometry());
                        Geometry geom = new PolygonHullSimplifier(union, false).getResult();
                        polygons.put(range, geom);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                has_failed = true;
            }
        }

        if (has_failed) {
            throw new Exception(error);
        }

        try {
            Set<Integer> visited = new HashSet<Integer>(10000);
            for (int i = 0; i < ranges.size(); i++) {
                double range = ranges.get(i);
                Geometry iso = polygons.get(range);

                Envelope env = iso.getEnvelopeInternal();
                Iterable<Integer> points = demand.getPointsInEnvelop(env);

                Geometry geom = new PolygonHullSimplifier(iso, false).getResult();

                for (int index : points) {
                    if (visited.contains(index)) {
                        continue;
                    }
                    Coordinate p = demand.getCoordinate(index);
                    int location = SimplePointInAreaLocator.locate(p, geom);
                    if (location == Location.INTERIOR) {
                        visited.add(index);
                        nearest_table[index] = -1;
                        ranges_table[index] = (float) range;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            has_failed = true;
            error = "Unexpected error during computation of nearest-neighbours.";
        }

        if (has_failed) {
            throw new Exception(error);
        }
        return new NNTable(nearest_table, ranges_table);
    }

    public IKNNTable requestKNearest(IDemandView demand, ISupplyView supply, int n, RoutingOptions options)
            throws Exception {
        int[][] nearest_table = new int[demand.pointCount()][n];
        float[][] ranges_table = new float[demand.pointCount()][n];
        for (int j = 0; j < demand.pointCount(); j++) {
            for (int i = 0; i < n; i++) {
                nearest_table[j][i] = -1;
                ranges_table[j][i] = -1;
            }
        }

        List<Double> ranges = options.getRanges();

        int point_count = supply.pointCount();
        double[][] facilities = new double[point_count][];
        for (int i = 0; i < point_count; i++) {
            Coordinate p = supply.getCoordinate(i);
            facilities[i] = new double[] { p.x, p.y };
        }

        BlockingQueue<IsochroneCollection> collection = this.requestIsochronesStream(facilities, ranges);
        boolean has_failed = false;
        String error = "";
        for (int j = 0; j < supply.pointCount(); j++) {
            IsochroneCollection isochrones;
            try {
                isochrones = collection.take();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            if (has_failed) {
                continue;
            }
            if (isochrones.isNull()) {
                has_failed = true;
                error = isochrones.getError();
                continue;
            }
            try {
                int facility_index = isochrones.getID();
                Set<Integer> visited = new HashSet<Integer>(10000);

                for (int i = 0; i < isochrones.getIsochronesCount(); i++) {
                    Isochrone isochrone = isochrones.getIsochrone(i);
                    double range = isochrone.getValue();
                    Geometry iso;
                    Geometry outer = isochrone.getGeometry();
                    if (i == 0) {
                        iso = outer;
                    } else {
                        Geometry inner = isochrones.getIsochrone(i - 1).getGeometry();
                        iso = outer.difference(inner);
                    }

                    Envelope env = iso.getEnvelopeInternal();
                    Iterable<Integer> points = demand.getPointsInEnvelop(env);

                    for (int index : points) {
                        if (visited.contains(index)) {
                            continue;
                        }
                        Coordinate p = demand.getCoordinate(index);
                        int location = SimplePointInAreaLocator.locate(p, iso);
                        if (location == Location.INTERIOR) {
                            visited.add(index);
                            // insert new range while keeping array-dimension sorted
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
                }
            } catch (Exception e) {
                e.printStackTrace();
                has_failed = true;
                error = "Unexpected error during computation of k-nearest-neighbours.";
            }
        }

        if (has_failed) {
            throw new Exception(error);
        }
        return new KNNTable(nearest_table, ranges_table);
    }

    public ICatchment requestCatchment(IDemandView demand, ISupplyView supply, double range, RoutingOptions options)
            throws Exception {
        List<Integer>[] accessibilities = new List[demand.pointCount()];

        List<Double> ranges = new ArrayList<>();
        ranges.add(range);

        int point_count = supply.pointCount();
        double[][] facilities = new double[point_count][];
        for (int i = 0; i < point_count; i++) {
            Coordinate p = supply.getCoordinate(i);
            facilities[i] = new double[] { p.x, p.y };
        }

        BlockingQueue<IsochroneCollection> collection = this.requestIsochronesStream(facilities, ranges);
        boolean has_failed = false;
        String error = "";
        for (int f = 0; f < supply.pointCount(); f++) {
            IsochroneCollection isochrones;
            try {
                isochrones = collection.take();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            if (has_failed) {
                continue;
            }
            if (isochrones.isNull()) {
                has_failed = true;
                error = isochrones.getError();
                continue;
            }
            try {
                int facility_index = isochrones.getID();

                Isochrone isochrone = isochrones.getIsochrone(0);
                Geometry iso = isochrone.getGeometry();

                Envelope env = iso.getEnvelopeInternal();
                Iterable<Integer> points = demand.getPointsInEnvelop(env);
                for (int index : points) {
                    Coordinate p = demand.getCoordinate(index);
                    int location = SimplePointInAreaLocator.locate(p, iso);
                    if (location == Location.INTERIOR) {
                        List<Integer> access;
                        if (accessibilities[index] == null) {
                            access = new ArrayList<Integer>();
                            accessibilities[index] = access;
                        } else {
                            access = accessibilities[index];
                        }
                        accessibilities[index].add(facility_index);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                has_failed = true;
                error = "Unexpected error during computation of catchments.";
            }
        }

        if (has_failed) {
            throw new Exception(error);
        }
        return new Catchment(accessibilities);
    }

    // private List<IsochroneCollection> requestIsochrones(double[][] locations,
    // List<Double> ranges) {
    // Map<String, Object> request = new HashMap();
    // request.put("locations", locations);
    // request.put("location_type", this.location_type);
    // request.put("range", ranges);
    // request.put("range_type", this.range_type);
    // request.put("units", "m");
    // request.put("smoothing", this.isochrone_smoothing);

    // try {
    // ObjectMapper objectMapper = new ObjectMapper();
    // String req = objectMapper.writeValueAsString(request);
    // String response = Util.sendPOST(this.url + "/v2/isochrones/" + this.profile +
    // "/geojson", req);

    // List<IsochroneCollection> iso_colls = new
    // ArrayList<IsochroneCollection>(locations.length);

    // JsonNode tree = objectMapper.readTree(response);
    // JsonNode features = tree.get("features");
    // List<Isochrone> isochrones = new ArrayList<Isochrone>();
    // Envelope envelope = null;
    // Coordinate center = new Coordinate(0, 0);
    // GeometryFactory geom_factory = new GeometryFactory();
    // for (JsonNode feature : features) {
    // JsonNode coords = feature.get("geometry").get("coordinates");
    // double[][] polygon = objectMapper.readValue(coords.toString(),
    // double[][][].class)[0];
    // Coordinate[] coordinates = new Coordinate[polygon.length];
    // for (int i = 0; i < polygon.length; i++) {
    // coordinates[i] = new Coordinate(polygon[i][0], polygon[i][1]);
    // }
    // Geometry geometry = geom_factory.createPolygon(coordinates);
    // Isochrone isochrone = new Isochrone(geometry,
    // feature.get("properties").get("value").asDouble());
    // isochrones.add(isochrone);
    // }
    // IsochroneCollection iso_coll = new IsochroneCollection(0, envelope,
    // isochrones, center);
    // iso_colls.add(iso_coll);

    // return iso_colls;
    // } catch (Exception e) {
    // return null;
    // }
    // }

    private BlockingQueue<IsochroneCollection> requestIsochronesStream(double[][] locations, List<Double> ranges) {

        BlockingQueue<IsochroneCollection> iso_colls = new ArrayBlockingQueue(10);

        for (int i = 0; i < locations.length; i++) {
            final int index = i;
            executor.submit(() -> {
                ObjectMapper objectMapper = new ObjectMapper();
                String response;
                try {
                    Map<String, Object> request = new HashMap();
                    request.put("location_type", this.location_type);
                    request.put("range", ranges);
                    request.put("range_type", this.range_type);
                    request.put("units", "m");
                    request.put("smoothing", this.isochrone_smoothing);
                    double[][] locs = { { locations[index][0], locations[index][1] } };
                    request.put("locations", locs);
                    String req = objectMapper.writeValueAsString(request);

                    response = Util.sendPOST(this.url + "/v2/isochrones/" + this.profile + "/geojson", req);
                } catch (Exception e) {
                    var iso = new IsochroneCollection("Request to ORS-failed. Make sure the server is running");
                    try {
                        iso_colls.put(iso);
                    } catch (Exception e_) {
                        e_.printStackTrace();
                    }
                    return;
                }
                try {
                    JsonNode tree = objectMapper.readTree(response);
                    JsonNode features = tree.get("features");
                    if (features != null) {
                        List<Isochrone> isochrones = new ArrayList<Isochrone>();
                        Envelope envelope = null;
                        Coordinate center = new Coordinate(0, 0);
                        GeometryFactory geom_factory = new GeometryFactory();
                        for (JsonNode feature : features) {
                            JsonNode coords = feature.get("geometry").get("coordinates");
                            double[][] polygon = objectMapper.readValue(coords.toString(), double[][][].class)[0];
                            Coordinate[] coordinates = new Coordinate[polygon.length];
                            for (int j = 0; j < polygon.length; j++) {
                                coordinates[j] = new Coordinate(polygon[j][0], polygon[j][1]);
                            }
                            Geometry geometry = geom_factory.createPolygon(coordinates);
                            Isochrone isochrone = new Isochrone(geometry,
                                    feature.get("properties").get("value").asDouble());
                            isochrones.add(isochrone);
                        }
                        IsochroneCollection iso_coll = new IsochroneCollection(index, envelope, isochrones, center);
                        try {
                            iso_colls.put(iso_coll);
                        } catch (Exception e_) {
                            e_.printStackTrace();
                        }
                        return;
                    } else {
                        JsonNode error = tree.get("error");
                        IsochroneCollection iso_coll;
                        if (error == null) {
                            iso_coll = new IsochroneCollection("Unknonwn response format from ORS.");
                        } else {
                            JsonNode message = error.get("message");
                            if (message == null) {
                                iso_coll = new IsochroneCollection("Unknonwn response format from ORS.");
                            } else {
                                iso_coll = new IsochroneCollection("Error at location {lon: "
                                        + locations[index][0] + ", lat: " + locations[index][1] + "}: "
                                        + message.asText());
                            }
                        }
                        try {
                            iso_colls.put(iso_coll);
                        } catch (Exception e_) {
                            e_.printStackTrace();
                        }
                        return;
                    }
                } catch (Exception e) {
                    try {
                        iso_colls.put(new IsochroneCollection("Something unexpected happened:" + e.getMessage()));
                    } catch (Exception e_) {
                        e_.printStackTrace();
                    }
                }
            });
        }

        return iso_colls;
    }

    private IsoRaster requestIsoRaster(double[][] locations, double max_range) {
        Map<String, Object> request = new HashMap();
        request.put("location_type", this.location_type);
        double[] ranges = { max_range };
        request.put("range", ranges);
        request.put("range_type", this.range_type);
        request.put("units", "m");
        request.put("consumer_type", "node_based");
        request.put("crs", "25832");
        request.put("precession", 1000);
        request.put("locations", locations);

        ObjectMapper objectMapper = new ObjectMapper();
        String response;
        try {
            String req = objectMapper.writeValueAsString(request);

            response = Util.sendPOST(this.url + "/v2/isoraster/" + this.profile, req);
        } catch (Exception e) {
            return new IsoRaster("Request to ORS-failed. Make sure the server is running");
        }
        try {
            IsoRaster raster = objectMapper.readValue(response, IsoRaster.class);
            raster.constructIndex();
            return raster;
        } catch (Exception e) {
            try {
                JsonNode tree = objectMapper.readTree(response);
                JsonNode error = tree.get("error");
                if (error == null) {
                    return new IsoRaster("Unknonwn response format from ORS.");
                }
                JsonNode message = error.get("message");
                if (message == null) {
                    return new IsoRaster("Unknonwn response format from ORS.");
                }
                return new IsoRaster(message.asText());
            } catch (Exception e1) {
                return new IsoRaster("Something unexpected happened:" + e1.getMessage());
            }
        }
    }

    private BlockingQueue<IsoRaster> requestIsoRasterStream(double[][] locations, double max_range) {

        BlockingQueue<IsoRaster> iso_rasters = new ArrayBlockingQueue(10);

        for (int i = 0; i < locations.length; i++) {
            final int index = i;
            executor.submit(() -> {
                Map<String, Object> request = new HashMap();
                request.put("location_type", this.location_type);
                double[] ranges = { max_range };
                request.put("range", ranges);
                request.put("range_type", this.location_type);
                request.put("units", "m");
                request.put("consumer_type", "node_based");
                request.put("crs", "25832");
                request.put("precession", 1000);

                ObjectMapper objectMapper = new ObjectMapper();
                String response;
                try {
                    double[][] locs = { { 0, 0 } };
                    locs[0][0] = locations[index][0];
                    locs[0][1] = locations[index][1];
                    request.put("locations", locs);
                    String req = objectMapper.writeValueAsString(request);

                    response = Util.sendPOST(this.url + "/v2/isoraster/" + this.profile, req);
                } catch (Exception e) {
                    IsoRaster iso = new IsoRaster("Request to ORS-failed. Make sure the server is running");
                    try {
                        iso_rasters.put(iso);
                    } catch (Exception e_) {
                        e_.printStackTrace();
                    }
                    return;
                }
                try {
                    IsoRaster raster = objectMapper.readValue(response, IsoRaster.class);
                    raster.constructIndex();
                    raster.setID(index);
                    try {
                        iso_rasters.put(raster);
                    } catch (Exception e_) {
                        e_.printStackTrace();
                    }
                    return;
                } catch (Exception e) {
                    try {
                        JsonNode tree = objectMapper.readTree(response);
                        JsonNode error = tree.get("error");
                        IsoRaster iso;
                        if (error == null) {
                            iso = new IsoRaster("Unknonwn response format from ORS.");
                        } else {
                            JsonNode message = error.get("message");
                            if (message == null) {
                                iso = new IsoRaster("Unknonwn response format from ORS.");
                            } else {
                                iso = new IsoRaster("Error at location {lon: "
                                        + locations[index][0] + ", lat: " + locations[index][1] + "}: "
                                        + message.asText());
                            }
                        }
                        try {
                            iso_rasters.put(iso);
                        } catch (Exception e_) {
                            e_.printStackTrace();
                        }
                        return;
                    } catch (Exception e1) {
                        var iso = new IsoRaster("Something unexpected happened:" + e1.getMessage());
                        try {
                            iso_rasters.put(iso);
                        } catch (Exception e_) {
                            e_.printStackTrace();
                        }
                        return;
                    }
                }
            });
        }

        return iso_rasters;
    }

    private Matrix requestMatrix(double[][] sources, double[][] destinations) {
        double[][] locations = new double[sources.length + destinations.length][];
        int[] source = new int[sources.length];
        int[] destination = new int[destinations.length];
        int c = 0;
        for (int i = 0; i < sources.length; i++) {
            source[i] = c;
            locations[c] = sources[i];
            c += 1;
        }
        for (int i = 0; i < destinations.length; i++) {
            destination[i] = c;
            locations[c] = destinations[i];
            c += 1;
        }

        Map<String, Object> request = new HashMap();
        request.put("locations", locations);
        request.put("sources", source);
        request.put("destinations", destination);
        request.put("units", "m");
        request.put("metrics", new String[] { this.range_type.equals("time") ? "duration" : "distance" });

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String response;
        try {
            String req = objectMapper.writeValueAsString(request);

            response = Util.sendPOST(this.url + "/v2/matrix/" + this.profile, req);
        } catch (Exception e) {
            return new Matrix("Request to ORS-failed. Make sure the server is running");
        }
        try {
            Matrix matrix = objectMapper.readValue(response, Matrix.class);
            return matrix;
        } catch (Exception e) {
            try {
                JsonNode tree = objectMapper.readTree(response);
                JsonNode error = tree.get("error");
                if (error == null) {
                    return new Matrix("Unknonwn response format from ORS.");
                }
                JsonNode message = error.get("message");
                if (message == null) {
                    return new Matrix("Unknonwn response format from ORS.");
                }
                return new Matrix(message.asText());
            } catch (Exception e1) {
                return new Matrix("Something unexpected happened:" + e1.getMessage());
            }
        }
    }
}
