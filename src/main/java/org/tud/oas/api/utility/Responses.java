package org.tud.oas.api.utility;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PopulationStoreResponse", description = """
        Population store response.
        """)
class PopulationStoreResponse {
    @Schema(name = "id", description = """
            Population view id.
            """, example = "sfjf-djfd-omsf-jjfd")
    public UUID id;

    PopulationStoreResponse(UUID id) {
        this.id = id;
    }
}

@Schema(name = "PopulationGetResponse", description = """
        Population get response.
        """)
class PopulationGetResponse {
    @Schema(name = "locations", description = """
            Locations of population points.
            """, example = "[[9.11, 51.23], [9.35, 50.98], [10.02, 52.10]]")
    public List<double[]> locations;

    @Schema(name = "weights", description = """
            Weights of population points (typically number of people living there).
            """, example = "[72, 29, 99]")
    public List<Double> weights;

    PopulationGetResponse(List<double[]> locations, List<Double> weights) {
        this.locations = locations;
        this.weights = weights;
    }
}
