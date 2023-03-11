package org.tud.oas.routing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.List;

public class ORSProvider implements IRoutingProvider {
    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    private final String url;

    public ORSProvider(String url) {
        this.url = url;
    }

    @Override
    public List<IsochroneCollection> requestIsochrones(Double[][] locations, List<Double> ranges) {
        Map<String, Object> request = new HashMap();
        request.put("locations", locations);
        request.put("location_type",  "destination");
        request.put("range", ranges);
        request.put("range_type", "time");
        request.put("units", "m");
        request.put("smoothing", 5.0);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String req = objectMapper.writeValueAsString(request);
            String response = Util.sendPOST(this.url + "/v2/isochrones/driving-car/geojson", req);
    
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
            IsochroneCollection iso_coll = new IsochroneCollection(envelope, isochrones, center);
            iso_colls.add(iso_coll);
    
            return iso_colls;
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public BlockingQueue<IsochroneCollection> requestIsochronesStream(Double[][] locations, List<Double> ranges) {

        BlockingQueue<IsochroneCollection> iso_colls = new ArrayBlockingQueue(10);

        for (int i=0; i<locations.length; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();

                    Map<String, Object> request = new HashMap();
                    request.put("location_type",  "destination");
                    request.put("range", ranges);
                    request.put("range_type", "time");
                    request.put("units", "m");
                    request.put("smoothing", 5.0);
                    double[][] locs = {{locations[index][0], locations[index][1]}};
                    request.put("locations", locs);
                    String req = objectMapper.writeValueAsString(request);

                    String response = Util.sendPOST(this.url + "/v2/isochrones/driving-car/geojson", req);

                    JsonNode tree = objectMapper.readTree(response);
                    JsonNode features = tree.get("features");
                    if (features == null) {
                        iso_colls.put(new IsochroneCollection(null, null, null));
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
                        Isochrone isochrone = new Isochrone(geometry, feature.get("properties").get("value").asDouble());
                        isochrones.add(isochrone);
                    }
                    IsochroneCollection iso_coll = new IsochroneCollection(envelope, isochrones, center);
                    iso_colls.put(iso_coll);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        return iso_colls;
    }

    @Override
    public List<IsoRaster> requestIsoRasters(Double[][] locations, double max_range) {
        Map<String, Object> request = new HashMap();
        request.put("location_type",  "destination");
        double[] ranges = { max_range };
        request.put("range", ranges);
        request.put("range_type", "time");
        request.put("units", "m");
        request.put("consumer_type", "node_based");
        request.put("crs", "25832");
        request.put("precession", 1000);

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            List<IsoRaster> iso_rasters = new ArrayList<IsoRaster>(locations.length);

            double[][] locs = {{0, 0}};
            for(int i=0; i<locations.length; i++) {
                locs[0][0] = locations[i][0];
                locs[0][1] = locations[i][1];
                request.put("locations", locs);
                String req = objectMapper.writeValueAsString(request);

                String response = Util.sendPOST(this.url + "/v2/isoraster/driving-car", req);

                IsoRaster raster = objectMapper.readValue(response, IsoRaster.class);
                raster.constructIndex();

                iso_rasters.add(raster);
            }

            return iso_rasters;
        }
        catch (Exception e) {
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