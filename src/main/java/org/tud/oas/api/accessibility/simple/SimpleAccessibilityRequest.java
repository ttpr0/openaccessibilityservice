package org.tud.oas.api.accessibility.simple;

import java.util.List;

import org.tud.oas.demand.DemandRequestParams;
import org.tud.oas.routing.RoutingRequestParams;
import org.tud.oas.supply.SupplyRequestParams;

/// <summary>
/// Simple-accessibility Request.
/// </summary>
public class SimpleAccessibilityRequest {
    /// <summary>
    /// Ranges (in sec) to be used by calculation.
    /// </summary>
    /// <example>[180, 360, 540, 720, 900]</example>
    public List<Double> ranges;

    public SupplyRequestParams supply;

    public DemandRequestParams demand;

    public RoutingRequestParams routing;
}