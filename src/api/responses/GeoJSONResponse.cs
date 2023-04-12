using System;
using NetTopologySuite.Geometries;
using DVAN.Population;

namespace DVAN.API
{
    public class GeoJSONResponse
    {
        public String type { get; set; }
        public GeoJSONFeature[] features { get; set; }

        public GeoJSONResponse(PopulationContainer population, float[] weights)
        {
            this.type = "FeatureCollection";
            GeoJSONFeature[] points = new GeoJSONPoint[weights.Length];
            for (int i = 0; i < points.Length; i++) {
                Coordinate p = population.getPoint(i);
                points[i] = new GeoJSONPoint((int)weights[i], p);
            }
            this.features = points;
        }
    }

    public abstract class GeoJSONFeature
    { }

    public class GeoJSONPoint : GeoJSONFeature
    {
        public string type = "Feature";
        public Properties properties;
        public Geometry geometry;

        public class Geometry
        {
            public String type = "Point";
            public Double[] coordinates;

            public Geometry(Coordinate point)
            {
                this.coordinates = new Double[2];
                this.coordinates[0] = point.X;
                this.coordinates[1] = point.Y;
            }
        }

        public class Properties
        {
            public int value;

            public Properties(int value)
            {
                this.value = value;
            }
        }

        public GeoJSONPoint(int value, Coordinate point)
        {
            this.properties = new Properties(value);
            this.geometry = new Geometry(point);
        }
    }
}
