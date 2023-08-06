package org.tud.oas.api.utility;

import java.util.UUID;

import org.tud.oas.demand.DemandRequestParams;

import com.fasterxml.jackson.annotation.JsonProperty;

/// <summary>
/// Request to store population view (from given parameters).
/// </summary>
class PopulationStoreRequest {
    @JsonProperty("population")
    public DemandRequestParams population;
}

/// <summary>
/// Request to get data from stored or internal population view.
/// </summary>
class PopulationGetRequest {
    /// <summary>
    /// Population view id.
    /// </summary>
    /// <example>sfjf-djfd-omsf-jjfd</example>
    @JsonProperty("population_id")
    public UUID population_id;

    @JsonProperty("population")
    public DemandRequestParams population;
}
