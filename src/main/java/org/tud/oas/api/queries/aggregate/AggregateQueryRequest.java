package org.tud.oas.api.queries.aggregate;

import java.util.UUID;

import org.tud.oas.demand.DemandRequestParams;
import org.tud.oas.routing.RoutingRequestParams;
import org.tud.oas.supply.SupplyRequestParams;

/// <summary>
/// Aggregate Query Request.
/// </summary>
public class AggregateQueryRequest {
    /// <summary>
    /// Session id. Used to reuse precomputed aggregation
    /// </summary>
    /// <example>smlf-dmxm-xdsd-yxdx</example>
    public UUID session_id;

    public DemandRequestParams demand;

    public RoutingRequestParams routing;

    public SupplyRequestParams supply;

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