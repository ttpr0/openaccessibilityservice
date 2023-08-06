package org.tud.oas.api.queries.n_nearest;

import java.util.List;
import java.util.UUID;

import org.tud.oas.demand.DemandRequestParams;
import org.tud.oas.routing.RoutingRequestParams;
import org.tud.oas.supply.SupplyRequestParams;

/// <summary>
/// N-Nearest Query Request.
/// </summary>
public class NNearestQueryRequest {
    /// <summary>
    /// Session id. Used to reuse precomputed aggregation
    /// </summary>
    /// <example>smlf-dmxm-xdsd-yxdx</example>
    public UUID session_id;

    public DemandRequestParams demand;

    public RoutingRequestParams routing;

    public SupplyRequestParams supply;

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