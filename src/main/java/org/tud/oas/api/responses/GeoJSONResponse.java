package org.tud.oas.api.responses;

import org.locationtech.jts.geom.Coordinate;
import org.tud.oas.demand.population.PopulationContainer;

public class GeoJSONResponse {
    public String type;
    public GeoJSONFeature[] features;

    public GeoJSONResponse(PopulationContainer population, float[] weights) {
        this.type = "FeatureCollection";
        GeoJSONFeature[] points = new GeoJSONPoint[weights.length];
        for (int i = 0; i < points.length; i++) {
            Coordinate p = population.getPoint(i);
            points[i] = new GeoJSONPoint((int) weights[i], p);
        }
        this.features = points;
    }
}

abstract class GeoJSONFeature {
}

class GeoJSONPoint extends GeoJSONFeature {
    public String type = "Feature";
    public Properties properties;
    public Geometry geometry;

    static class Geometry {
        public String type = "Point";
        public double[] coordinates;

        public Geometry(Coordinate point) {
            this.coordinates = new double[2];
            this.coordinates[0] = point.getX();
            this.coordinates[1] = point.getY();
        }
    }

    static class Properties {
        public int value;

        public Properties(int value) {
            this.value = value;
        }
    }

    public GeoJSONPoint(int value, Coordinate point) {
        this.properties = new Properties(value);
        this.geometry = new Geometry(point);
    }
}
