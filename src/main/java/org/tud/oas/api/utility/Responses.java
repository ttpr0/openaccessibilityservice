package org.tud.oas.api.utility;

import java.util.List;
import java.util.UUID;

/// <summary>
/// Population store response.
/// </summary>
class PopulationStoreResponse {
    /// <summary>
    /// Population view id.
    /// </summary>
    /// <example>sfjf-djfd-omsf-jjfd</example>
    public UUID id;

    PopulationStoreResponse(UUID id) {
        this.id = id;
    }
}

/// <summary>
/// Population get response.
/// </summary>
class PopulationGetResponse {
    /// <summary>
    /// Locations of population points.
    /// </summary>
    /// <example>[[9.11, 51.23], [9.35, 50.98], [10.02, 52.10]]</example>
    public List<double[]> locations;

    /// <summary>
    /// Weights of population points (typically number of people living there).
    /// </summary>
    /// <example>[72, 29, 99]</example>
    public List<Double> weights;

    PopulationGetResponse(List<double[]> locations, List<Double> weights) {
        this.locations = locations;
        this.weights = weights;
    }
}
