package org.tud.oas.api.accessibility.gravity;

import java.util.List;

import org.tud.oas.demand.DemandRequestParams;
import org.tud.oas.routing.RoutingRequestParams;
import org.tud.oas.supply.SupplyRequestParams;

/// <summary>
/// Gravity Request.
/// </summary>
public class GravityRequest {
    /// <summary>
    /// Ranges (in sec) to be used by calculation.
    /// </summary>
    /// <example>[180, 360, 540, 720, 900]</example>
    public List<Double> ranges;

    /// <summary>
    /// Distance-Decay for every catchment (ranges).
    /// </summary>
    /// <example>[1.0, 0.7, 0.5, 0.3, 0.1]</example>
    public List<Double> range_factors;

    public DemandRequestParams demand;

    public SupplyRequestParams supply;

    public RoutingRequestParams routing;
}
