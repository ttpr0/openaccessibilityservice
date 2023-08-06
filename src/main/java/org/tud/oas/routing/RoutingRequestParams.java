package org.tud.oas.routing;

import com.fasterxml.jackson.annotation.JsonProperty;

/// <summary>
/// Parameters for the routing.
/// Parameters will be used for queries on routing backend.
/// All Parameters are optional, is usefull defaults will be set.
/// </summary>
public class RoutingRequestParams {
    // *************************************
    // standard routing params
    // *************************************
    /// <summary>
    /// Routing profile to be used (e.g. driving-car, ...).
    /// </summary>
    /// <example>driving-car</example>
    @JsonProperty("profile")
    public String profile;

    /// <summary>
    /// Routing metric (travel-time or distance).
    /// </summary>
    /// <example>time</example>
    @JsonProperty("range_type")
    public String range_type;

    // *************************************
    // additional routing params
    // *************************************
    /// <summary>
    /// Sets weather borders should be avoided (cross country).
    /// </summary>
    /// <example>all</example>
    @JsonProperty("avoid_borders")
    public String avoid_borders;

    /// <summary>
    /// Sets which road segments should be avoided.
    /// </summary>
    /// <example>["highway", "ferries"]</example>
    @JsonProperty("avoid_features")
    public String[] avoid_features;

    /// <summary>
    /// Sets an area to avid while routing.
    /// Polygon or MultiPolygon formatted as GeoJSON.
    /// </summary>
    /// <example>{"type": "Polygon", "coordinates": [[9.1, 50.1], [9.9, 50.1], [9.4,
    /// 50.8]]}</example>
    @JsonProperty("avoid_polygons")
    public Object avoid_polygons;

    // *************************************
    // isochrone params
    // *************************************
    /// <summary>
    /// Sets weather input locations should be used as origin or destination of
    // travel.
    /// </summary>
    /// <example>start</example>
    @JsonProperty("location_type")
    public String location_type;

    /// <summary>
    /// Sets how much isochrones should be smoothed.
    /// High values lead to a high degree of smoothing.
    /// </summary>
    /// <example>25</example>
    @JsonProperty("isochrone_smoothing")
    public Float isochrone_smoothing;
}
