package org.tud.oas.supply;

import com.fasterxml.jackson.annotation.JsonProperty;

/// <summary>
/// Supply Request Parameters.
/// Supply view will be created from locations and weights.
/// </summary>
public class SupplyRequestParams {
    // *************************************
    // create new view from locations and weights
    // *************************************
    /// <summary>
    /// Locations of supply points (in geographic coordinates).
    /// </summary>
    /// <example>[[9.11, 51.23], [9.35, 50.98], [10.02, 52.10]]</example>
    @JsonProperty("supply_locations")
    public double[][] supply_locations;

    /// <summary>
    /// Weights of supply points (e.g. working times equivalent of doctors).
    /// Weight for every point in "supply_locations".
    /// If not provided, every supply point will be weighted with 1.
    /// </summary>
    /// <example>[91, 34, 72]</example>
    @JsonProperty("supply_weights")
    public double[] supply_weights;
}
