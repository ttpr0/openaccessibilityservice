package org.tud.oas.api.queries.n_nearest;

import java.util.List;
import java.util.UUID;

import org.tud.oas.population.PopulationRequestParams;
import org.tud.oas.routing.RoutingRequestParams;

/// <summary>
/// N-Nearest Query Request.
/// </summary>
public class NNearestQueryRequest {
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
    /// Range-type to be used (One of "continuus", "discrete").
    /// </summary>
    /// <example>discrete</example>
    public String range_type;

    /// <summary>
    /// Maximum range (for continuus range_type) in seconds.
    /// </summary>
    /// <example>900</example>
    public Double range_max;

    /// <summary>
    /// Ranges (in sec) to be used by calculation.
    /// </summary>
    /// <example>[180, 360, 540, 720, 900]</example>
    public List<Double> ranges;

    /// <summary>
    /// Calculation mode (one of "mean", "median", "min", "max").
    /// </summary>
    /// <example>mean</example>
    public String compute_type;

    /// <summary>
    /// Number (n) of closest facilities to be used.
    /// </summary>
    /// <example>3</example>
    public int facility_count;
}