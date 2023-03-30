using System;
using NetTopologySuite.Geometries;
using DVAN.Population;

namespace DVAN.API
{
    public class FCAGeoJSONResponse
    {
        public String type { get; set; }
        public GeoJsonFeature[] features { get; set; }

        public FCAGeoJSONResponse(PopulationContainer population, float[] weights)
        {
            this.type = "FeatureCollection";
            GeoJsonFeature[] points = new GeoJsonPoint[weights.Length];
            for (int i = 0; i < points.Length; i++) {
                Coordinate p = population.getPoint(i);
                points[i] = new GeoJsonPoint((int)weights[i], p);
            }
            this.features = points;
        }

    }
}
