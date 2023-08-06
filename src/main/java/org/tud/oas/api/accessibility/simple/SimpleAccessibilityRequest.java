package org.tud.oas.api.accessibility.simple;

import java.util.List;

import org.tud.oas.population.PopulationRequestParams;
import org.tud.oas.routing.RoutingRequestParams;

/// <summary>
/// Simple-accessibility Request.
/// </summary>
public class SimpleAccessibilityRequest {
    /// <summary>
    /// Ranges (in sec) to be used by calculation.
    /// </summary>
    /// <example>[180, 360, 540, 720, 900]</example>
    public List<Double> ranges;

    /// <summary>
    /// Facility Locations in geographic coordinates.
    /// </summary>
    /// <example>[[9.11, 51.23], [9.35, 50.98], [10.02, 52.10]]</example>
    public double[][] facility_locations;

    public PopulationRequestParams population;

    public RoutingRequestParams routing;
}