package org.tud.oas.routing;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GridFeature {
    @JsonProperty("x")
    public float x;
    @JsonProperty("y")
    public float y;
    @JsonProperty("value")
    public GridValue value;

    public GridFeature() {}
    
    public GridFeature(float x, float y, GridValue value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }
}