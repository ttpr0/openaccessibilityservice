package org.tud.oas.api.accessibility;

import java.util.List;

public class GridResponse {
    public String crs;
    public float[] extend;
    public int[] size;
    public List<Feature> features;

    public GridResponse(List<Feature> features, String crs, float[] extend, int[] size) {
        this.crs = crs;
        this.extend = extend;
        this.size = size;
        this.features = features;
    }
}

class Feature
{
    public float x;
    public float y;
    public Object value;
    
    public Feature(float x, float y, Object value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }
}
