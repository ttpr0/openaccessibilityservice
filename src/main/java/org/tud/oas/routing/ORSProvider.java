package org.tud.oas.routing;

import org.heigit.ors.api.requests.isochrones.IsochronesRequest;
import org.heigit.ors.api.requests.isochrones.IsochronesRequestEnums;
import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.api.requests.common.APIEnums.Profile;

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
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ORSProvider implements IRoutingProvider {

    @Override
    public List<IsochroneCollection> requestIsochrones(Double[][] locations, List<Double> ranges) {
        Map<String, Object> request = new HashMap();
        request.put("locations", locations);
        request.put("location_type",  IsochronesRequestEnums.LocationType.DESTINATION);
        request.put("range", ranges);
        request.put("range_type", IsochronesRequestEnums.RangeType.TIME);
        request.put("units", APIEnums.Units.METRES);
        request.put("smoothing", 5.0);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String req = objectMapper.writeValueAsString(request);
            String response = Util.sendPOST(req);

            System.out.println(response);
    
            var iso_colls = new ArrayList<IsochroneCollection>(locations.length);
    
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
    static String sendPOST(String request_body) throws Exception {
        URL obj = new URL("http://localhost:8082/v2/isochrones/driving-car/geojson");
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        try(OutputStream os = con.getOutputStream()) {
            byte[] input = request_body.getBytes("utf-8");
            os.write(input, 0, input.length);			
        }

        try(BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }
}