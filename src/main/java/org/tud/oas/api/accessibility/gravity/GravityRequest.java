package org.tud.oas.api.accessibility.gravity;

import java.util.List;

import org.tud.oas.accessibility.distance_decay.DecayRequestParams;
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

    public DecayRequestParams distance_decay;

    public DemandRequestParams demand;

    public SupplyRequestParams supply;

    public RoutingRequestParams routing;
}
