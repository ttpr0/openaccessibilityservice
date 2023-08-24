package org.tud.oas.responses;

import java.util.List;
import java.util.UUID;

/// <summary>
/// Grid response.
/// Can be used to visualize grid as map-layer.
/// </summary>
public class GridResponse {
    public UUID id;

    /// <summary>
    /// Grid coordinate reference system.
    /// </summary>
    /// <example>EPSG:25832</example>
    public String crs;

    /// <summary>
    /// Grid extend ([minx, miny, maxx, maxy]).
    /// </summary>
    /// <example>[9.11, 50.98, 10.23, 52.09]</example>
    public float[] extend;

    /// <summary>
    /// Grid size ([rows, cols]).
    /// </summary>
    /// <example>[20, 20]</example>
    public int[] size;

    /// <summary>
    /// List of grid features.
    /// </summary>
    public List<GridFeature> features;

    public GridResponse(List<GridFeature> features, String crs, float[] extend, int[] size) {
        this.id = null;
        this.crs = crs;
        this.extend = extend;
        this.size = size;
        this.features = features;
    }
}

class GridFeature {
    /// <summary>
    /// Feature x coordinate (in specified crs).
    /// </summary>
    /// <example>500233</example>
    public float x;

    /// <summary>
    /// Feature y coordinate (in specified crs).
    /// </summary>
    /// <example>534395</example>
    public float y;

    /// <summary>
    /// Feature value at "x" and "y" coordinate.
    /// Contains object with keys and values (e.g. {first: 10, second: 23, multi:
    /// 100}).
    /// </summary>
    /// <example>{}</example>
    public Object value;

    public GridFeature(float x, float y, Object value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }
}
