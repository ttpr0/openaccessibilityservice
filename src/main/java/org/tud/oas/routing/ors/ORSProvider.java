package org.tud.oas.routing.ors;

import org.tud.oas.routing.INNTable;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.ITDMatrix;
import org.tud.oas.routing.TDMatrix;
import org.tud.oas.supply.ISupplyView;
import org.tud.oas.routing.NNTable;
import org.tud.oas.routing.KNNTable;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.routing.Catchment;
import org.tud.oas.routing.ICatchment;
import org.tud.oas.routing.IKNNTable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.algorithm.locate.SimplePointInAreaLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.simplify.PolygonHullSimplifier;
import org.locationtech.jts.geom.Envelope;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
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
import java.util.Dictionary;

public class ORSProvider implements IRoutingProvider {
    private static ExecutorService executor = Executors.newFixedThreadPool(10);

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

    public void setOption(String name, Object value) {
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

    public ITDMatrix requestTDMatrix(IDemandView demand, ISupplyView supply, List<Double> ranges,
            String mode) {
        switch (mode) {
            case "isochrones":
                return this.requestMatrixIsochrones(demand, supply, ranges);
            case "matrix":
                return this.requestMatrixMatrix(demand, supply, ranges);
            case "isoraster":
                return this.requestMatrixIsoraster(demand, supply, ranges);
            default:
                return this.requestMatrixMatrix(demand, supply, ranges);
        }
    }

    private ITDMatrix requestMatrixIsochrones(IDemandView demand, ISupplyView supply, List<Double> ranges) {
        double[][] matrix = new double[supply.pointCount()][demand.pointCount()];
        for (int i = 0; i < supply.pointCount(); i++) {
            for (int j = 0; j < demand.pointCount(); j++) {
                matrix[i][j] = 9999;
            }
        }

        int point_count = supply.pointCount();
        double[][] facilities = new double[point_count][];
        for (int i = 0; i < point_count; i++) {
            Coordinate p = supply.getCoordinate(i);
            facilities[i] = new double[] { p.x, p.y };
        }

        BlockingQueue<IsochroneCollection> collection = this.requestIsochronesStream(facilities, ranges);
        for (int f = 0; f < supply.pointCount(); f++) {
            IsochroneCollection isochrones;
            try {
                isochrones = collection.take();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            if (isochrones.isNull()) {
                continue;
            }
            int facility_index = isochrones.getID();

            Set<Integer> visited = new HashSet<Integer>(10000);
            for (int i = 0; i < isochrones.getIsochronesCount(); i++) {
                Isochrone isochrone = isochrones.getIsochrone(i);
                double range = isochrone.getValue();

                Geometry iso = isochrone.getGeometry();
                Envelope env = iso.getEnvelopeInternal();

                List<Integer> points = demand.getPointsInEnvelop(env);

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
        }

        return new TDMatrix(matrix);
    }

    private ITDMatrix requestMatrixMatrix(IDemandView demand, ISupplyView supply, List<Double> ranges) {
        double max_range = ranges.get(ranges.size() - 1);

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
        if (matrix == null || matrix.durations == null) {
            return null;
        }

        return new TDMatrix(matrix.durations);
    }

    private ITDMatrix requestMatrixIsoraster(IDemandView demand, ISupplyView supply, List<Double> ranges) {
        double[][] matrix = new double[supply.pointCount()][demand.pointCount()];
        for (int i = 0; i < supply.pointCount(); i++) {
            for (int j = 0; j < demand.pointCount(); j++) {
                matrix[i][j] = 9999;
            }
        }

        int point_count = supply.pointCount();
        double[][] facilities = new double[point_count][];
        for (int i = 0; i < point_count; i++) {
            Coordinate p = supply.getCoordinate(i);
            facilities[i] = new double[] { p.x, p.y };
        }

        double max_range = ranges.get(ranges.size() - 1);
        IsoRaster isoraster = this.requestIsoRaster(facilities, max_range);
        if (isoraster == null) {
            return null;
        }

        double[][] extend = isoraster.getEnvelope();
        Envelope env = new Envelope(extend[0][0], extend[3][0], extend[2][1], extend[1][1]);
        List<Integer> points = demand.getPointsInEnvelop(env);

        for (int index : points) {
            Coordinate p = demand.getCoordinate(index, "EPSG:25832");
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

    public INNTable requestNearest(IDemandView demand, ISupplyView supply, List<Double> ranges,
            String mode) {
        int[] nearest_table = new int[demand.pointCount()];
        float[] ranges_table = new float[demand.pointCount()];
        for (int j = 0; j < demand.pointCount(); j++) {
            nearest_table[j] = -1;
            ranges_table[j] = 9999;
        }

        int point_count = supply.pointCount();
        double[][] facilities = new double[point_count][];
        for (int i = 0; i < point_count; i++) {
            Coordinate p = supply.getCoordinate(i);
            facilities[i] = new double[] { p.x, p.y };
        }

        Map<Double, Geometry> polygons = new HashMap<Double, Geometry>(ranges.size());
        BlockingQueue<IsochroneCollection> collection = this.requestIsochronesStream(facilities, ranges);
        for (int j = 0; j < supply.pointCount(); j++) {
            IsochroneCollection isochrones;
            try {
                isochrones = collection.take();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            if (isochrones.isNull()) {
                continue;
            }

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
        }

        Set<Integer> visited = new HashSet<Integer>(10000);
        for (int i = 0; i < ranges.size(); i++) {
            double range = ranges.get(i);
            Geometry iso = polygons.get(range);

            Envelope env = iso.getEnvelopeInternal();
            List<Integer> points = demand.getPointsInEnvelop(env);

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

        return new NNTable(nearest_table, ranges_table);
    }

    public IKNNTable requestKNearest(IDemandView demand, ISupplyView supply, List<Double> ranges, int n,
            String mode) {
        int[][] nearest_table = new int[demand.pointCount()][n];
        float[][] ranges_table = new float[demand.pointCount()][n];
        for (int j = 0; j < demand.pointCount(); j++) {
            for (int i = 0; i < n; i++) {
                nearest_table[j][i] = -1;
                ranges_table[j][i] = 9999;
            }
        }

        int point_count = supply.pointCount();
        double[][] facilities = new double[point_count][];
        for (int i = 0; i < point_count; i++) {
            Coordinate p = supply.getCoordinate(i);
            facilities[i] = new double[] { p.x, p.y };
        }

        BlockingQueue<IsochroneCollection> collection = this.requestIsochronesStream(facilities, ranges);
        for (int j = 0; j < supply.pointCount(); j++) {
            IsochroneCollection isochrones;
            try {
                isochrones = collection.take();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            if (isochrones.isNull()) {
                continue;
            }

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
                List<Integer> points = demand.getPointsInEnvelop(env);

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
                        if (last_range > range || last_range == 9999) {
                            nearest_table[index][n - 1] = facility_index;
                            ranges_table[index][n - 1] = (float) range;
                            for (int k = n - 2; k >= 0; k--) {
                                float curr_range = ranges_table[index][k];
                                float prev_range = ranges_table[index][k + 1];
                                if (curr_range > prev_range || curr_range == 9999) {
                                    nearest_table[index][k] = nearest_table[index][k + 1];
                                    ranges_table[index][k] = prev_range;
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        return new KNNTable(nearest_table, ranges_table);
    }

    public ICatchment requestCatchment(IDemandView demand, ISupplyView supply, double range, String mode) {
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
        for (int f = 0; f < supply.pointCount(); f++) {
            IsochroneCollection isochrones;
            try {
                isochrones = collection.take();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            if (isochrones.isNull()) {
                continue;
            }
            int facility_index = isochrones.getID();

            Isochrone isochrone = isochrones.getIsochrone(0);
            Geometry iso = isochrone.getGeometry();

            Envelope env = iso.getEnvelopeInternal();
            List<Integer> points = demand.getPointsInEnvelop(env);
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
        }

        return new Catchment(accessibilities);
    }

    public List<IsochroneCollection> requestIsochrones(double[][] locations, List<Double> ranges) {
        Map<String, Object> request = new HashMap();
        request.put("locations", locations);
        request.put("location_type", this.location_type);
        request.put("range", ranges);
        request.put("range_type", this.range_type);
        request.put("units", "m");
        request.put("smoothing", this.isochrone_smoothing);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String req = objectMapper.writeValueAsString(request);
            String response = Util.sendPOST(this.url + "/v2/isochrones/" + this.profile + "/geojson", req);

            List<IsochroneCollection> iso_colls = new ArrayList<IsochroneCollection>(locations.length);

            JsonNode tree = objectMapper.readTree(response);
            JsonNode features = tree.get("features");
            List<Isochrone> isochrones = new ArrayList<Isochrone>();
            Envelope envelope = null;
            Coordinate center = new Coordinate(0, 0);
            GeometryFactory geom_factory = new GeometryFactory();
            for (JsonNode feature : features) {
                JsonNode coords = feature.get("geometry").get("coordinates");
                double[][] polygon = objectMapper.readValue(coords.toString(), double[][][].class)[0];
                Coordinate[] coordinates = new Coordinate[polygon.length];
                for (int i = 0; i < polygon.length; i++) {
                    coordinates[i] = new Coordinate(polygon[i][0], polygon[i][1]);
                }
                Geometry geometry = geom_factory.createPolygon(coordinates);
                Isochrone isochrone = new Isochrone(geometry, feature.get("properties").get("value").asDouble());
                isochrones.add(isochrone);
            }
            IsochroneCollection iso_coll = new IsochroneCollection(0, envelope, isochrones, center);
            iso_colls.add(iso_coll);

            return iso_colls;
        } catch (Exception e) {
            return null;
        }
    }

    public BlockingQueue<IsochroneCollection> requestIsochronesStream(double[][] locations, List<Double> ranges) {

        BlockingQueue<IsochroneCollection> iso_colls = new ArrayBlockingQueue(10);

        for (int i = 0; i < locations.length; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();

                    Map<String, Object> request = new HashMap();
                    request.put("location_type", this.location_type);
                    request.put("range", ranges);
                    request.put("range_type", this.range_type);
                    request.put("units", "m");
                    request.put("smoothing", this.isochrone_smoothing);
                    double[][] locs = { { locations[index][0], locations[index][1] } };
                    request.put("locations", locs);
                    String req = objectMapper.writeValueAsString(request);

                    String response = Util.sendPOST(this.url + "/v2/isochrones/" + this.profile + "/geojson", req);

                    JsonNode tree = objectMapper.readTree(response);
                    JsonNode features = tree.get("features");
                    if (features == null) {
                        iso_colls.put(new IsochroneCollection(index, null, null, null));
                        return;
                    }
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
                    iso_colls.put(iso_coll);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        return iso_colls;
    }

    public IsoRaster requestIsoRaster(double[][] locations, double max_range) {
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

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String req = objectMapper.writeValueAsString(request);

            String response = Util.sendPOST(this.url + "/v2/isoraster/" + this.profile, req);

            IsoRaster raster = objectMapper.readValue(response, IsoRaster.class);
            raster.constructIndex();

            return raster;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public BlockingQueue<IsoRaster> requestIsoRasterStream(double[][] locations, double max_range) {

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

                try {
                    ObjectMapper objectMapper = new ObjectMapper();

                    double[][] locs = { { 0, 0 } };
                    locs[0][0] = locations[index][0];
                    locs[0][1] = locations[index][1];
                    request.put("locations", locs);
                    String req = objectMapper.writeValueAsString(request);

                    String response = Util.sendPOST(this.url + "/v2/isoraster/" + this.profile, req);

                    IsoRaster raster = objectMapper.readValue(response, IsoRaster.class);
                    raster.constructIndex();
                    raster.setID(index);

                    iso_rasters.put(raster);
                } catch (Exception e) {
                    e.printStackTrace();
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
        request.put("metrics", this.range_type == "time" ? "duration" : this.range_type);

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            String req = objectMapper.writeValueAsString(request);

            String response = Util.sendPOST(this.url + "/v2/matrix/" + this.profile, req);

            Matrix matrix = objectMapper.readValue(response, Matrix.class);

            return matrix;
        } catch (Exception e) {
            return null;
        }
    }
}

class Util {
    static String sendPOST(String url, String request_body) throws Exception {
        Builder builder = HttpRequest.newBuilder();
        builder.uri(new URI(url));
        builder.header("Content-Type", "application/json");
        builder.POST(BodyPublishers.ofString(request_body));
        HttpRequest request = builder.build();

        HttpClient client = HttpClient.newHttpClient();
        BodyHandler<String> bodyHandler = BodyHandlers.ofString();
        HttpResponse<String> response = client.send(request, bodyHandler);

        return response.body();
    }
}