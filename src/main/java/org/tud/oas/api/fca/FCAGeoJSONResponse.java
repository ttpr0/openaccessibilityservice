package org.tud.oas.api.fca;

import org.locationtech.jts.geom.Coordinate;
import org.tud.oas.population.Population;

public class FCAGeoJSONResponse {
    public String type = "FeatureCollection"; 
    public GeoJsonFeature[] features;
    
    public FCAGeoJSONResponse(Population population, float[] weights)
    {
        GeoJsonFeature[] points = new GeoJsonPoint[weights.length];
        for (int i=0; i<points.length; i++) 
        {
            Coordinate p = population.getPoint(i);
            points[i] = new GeoJsonPoint((int)weights[i], p);
        }
        this.features = points;
    }

}
