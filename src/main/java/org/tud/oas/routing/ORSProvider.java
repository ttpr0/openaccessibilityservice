package org.tud.oas.routing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Envelope;

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
import java.util.ArrayList;
import java.util.List;

public class ORSProvider implements IRoutingProvider {
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
}

class Util {
    private static BodyHandler<String> bodyHandler = BodyHandlers.ofString();
    private static HttpClient client = HttpClient.newHttpClient();

    static String sendPOST(String url, String request_body) throws Exception {
        Builder builder = HttpRequest.newBuilder();
        builder.uri(new URI(url));
        builder.header("Content-Type", "application/json");
        builder.POST(BodyPublishers.ofString(request_body));
        HttpRequest request = builder.build();

        HttpResponse<String> response = client.send(request, bodyHandler);

        return response.body();
    }
}