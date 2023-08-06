package org.tud.oas.api.accessibility.multi_criteria;

import java.util.List;
import java.util.Map;

import org.tud.oas.population.PopulationRequestParams;
import org.tud.oas.routing.RoutingRequestParams;

/// <summary>
/// Multi-criteria Request.
/// </summary>
public class MultiCriteriaRequest {
    /// <summary>
    /// infrastructure parameters.
    /// </summary>
    public Map<String, InfrastructureParams> infrastructures;

    public PopulationRequestParams population;

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

    /// <summary>
    /// Facility Locations in geographic coordinates.
    /// </summary>
    /// <example>[[9.11, 51.23], [9.35, 50.98], [10.02, 52.10]]</example>
    public double[][] facility_locations;
}
