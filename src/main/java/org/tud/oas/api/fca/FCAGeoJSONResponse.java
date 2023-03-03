package org.tud.oas.api.fca;

import org.tud.oas.population.Population;
import org.tud.oas.population.PopulationPoint;

public class FCAGeoJSONResponse {
    public String type = "FeatureCollection"; 
    public GeoJsonFeature[] features;
    
    public FCAGeoJSONResponse(Population population, float[] weights)
    {
        GeoJsonFeature[] points = new GeoJsonPoint[weights.length];
        for (int i=0; i<points.length; i++) 
        {
            PopulationPoint p = population.getPoint(i);
            points[i] = new GeoJsonPoint((int)weights[i], p.getPoint().getCoordinate());
        }
        this.features = points;
    }

}
