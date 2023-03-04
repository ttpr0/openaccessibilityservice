package org.tud.oas.api.fca;

import org.locationtech.jts.geom.Coordinate;

public class GeoJsonPoint extends GeoJsonFeature {
    public String type = "Feature";
    public properties properties;
    public geometry geometry;

    public class geometry
    {
        public String type = "Point";
        public Double[] coordinates;

        public geometry(Coordinate point)
        {
            this.coordinates = new Double[2];
            this.coordinates[0] = point.x;
            this.coordinates[1] = point.y;
        }
    }

    public class properties
    {
        public int value;

        public properties(int value)
        {
            this.value = value;
        }
    }

    public GeoJsonPoint(int value, Coordinate point)
    {
        this.properties = new properties(value);
        this.geometry = new geometry(point);
    }
}
