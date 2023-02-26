package org.tud.oas.api.accessibility;

import java.util.List;

public class GridResponse {
    public String crs;
    public float[] extend;
    public int[] size;
    public List<GridFeature> features;

    public GridResponse(List<GridFeature> features, String crs, float[] extend, int[] size) {
        this.crs = crs;
        this.extend = extend;
        this.size = size;
        this.features = features;
    }
}
