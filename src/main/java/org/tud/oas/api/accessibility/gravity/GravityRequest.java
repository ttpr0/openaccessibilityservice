package org.tud.oas.api.accessibility.gravity;

import java.util.List;

import org.tud.oas.population.PopulationRequestParams;
import org.tud.oas.routing.RoutingRequestParams;

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

    /// <summary>
    /// Facility Locations in geographic coordinates.
    /// </summary>
    /// <example>[[9.11, 51.23], [9.35, 50.98], [10.02, 52.10]]</example>
    public double[][] facility_locations;

    public PopulationRequestParams population;

    public RoutingRequestParams routing;
}
