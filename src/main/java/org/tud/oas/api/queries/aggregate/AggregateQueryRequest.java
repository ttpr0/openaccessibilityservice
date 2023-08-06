package org.tud.oas.api.queries.aggregate;

import java.util.UUID;

import org.tud.oas.population.PopulationRequestParams;
import org.tud.oas.routing.RoutingRequestParams;

/// <summary>
/// Aggregate Query Request.
/// </summary>
public class AggregateQueryRequest {
    /// <summary>
    /// Session id. Used to reuse precomputed aggregation
    /// </summary>
    /// <example>smlf-dmxm-xdsd-yxdx</example>
    public UUID session_id;

    public PopulationRequestParams population;

    public RoutingRequestParams routing;

    /// <summary>
    /// Facility Locations in geographic coordinates.
    /// </summary>
    /// <example>[[9.11, 51.23], [9.35, 50.98], [10.02, 52.10]]</example>
    public double[][] facility_locations;

    /// <summary>
    /// Facility values that will be aggregated.
    /// </summary>
    /// <example>[100.1, 23.8, 107.8]</example>
    public double[] facility_values;

    /// <summary>
    /// Catchment range in seconds.
    /// </summary>
    /// <example>900</example>
    public Double range;

    /// <summary>
    /// Calculation mode (one of "mean", "median", "min", "max").
    /// </summary>
    /// <example>mean</example>
    public String compute_type;
}