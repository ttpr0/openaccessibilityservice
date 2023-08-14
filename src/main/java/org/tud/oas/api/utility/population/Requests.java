package org.tud.oas.api.utility.population;

import java.util.UUID;

import org.tud.oas.demand.DemandRequestParams;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PopulationStoreRequest", description = """
        Request to store population view (from given parameters).
        """)
class PopulationStoreRequest {
    @Schema(name = "population", description = """
            Parameters of population-view to be stored.
            """)
    @JsonProperty("population")
    public DemandRequestParams population;
}

@Schema(name = "PopulationGetRequest", description = """
        Request to get data from stored or internal population view.
        """)
class PopulationGetRequest {
    @Schema(name = "population_id", description = """
            Population view id.
            """, example = "sfjf-djfd-omsf-jjfd")
    @JsonProperty("population_id")
    public UUID population_id;

    @Schema(name = "population", description = """
            Parameters of population-view to be retrived.
            """)
    @JsonProperty("population")
    public DemandRequestParams population;
}
