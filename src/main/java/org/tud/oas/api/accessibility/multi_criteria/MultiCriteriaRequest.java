package org.tud.oas.api.accessibility.multi_criteria;

import java.util.List;
import java.util.Map;

import org.tud.oas.accessibility.distance_decay.DecayRequestParams;
import org.tud.oas.demand.DemandRequestParams;
import org.tud.oas.routing.RoutingRequestParams;
import org.tud.oas.supply.SupplyRequestParams;

/// <summary>
/// Multi-criteria Request.
/// </summary>
public class MultiCriteriaRequest {
    /// <summary>
    /// infrastructure parameters.
    /// </summary>
    public Map<String, InfrastructureParams> infrastructures;

    public DemandRequestParams demand;

    public RoutingRequestParams routing;
}

/// <summary>
/// Infrastructure parameter.
/// </summary>
class InfrastructureParams {
    /// <summary>
    /// Weight of infrastructure in multi-criteria..
    /// </summary>
    /// <example>0.8</example>
    public double infrastructure_weight;

    public DecayRequestParams decay;

    public SupplyRequestParams supply;
}
