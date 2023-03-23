using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;

namespace DVAN.API
{
    public abstract class GeoJsonFeature
    {

    }

    public class GeoJsonPoint : GeoJsonFeature 
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

        public GeoJsonPoint(int value, Coordinate point)
        {
            this.properties = new Properties(value);
            this.geometry = new Geometry(point);
        }
    }

}