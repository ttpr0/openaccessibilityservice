package org.tud.oas.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SupplyRequestParams", description = """
                Supply Request Parameters.
                Supply view will be created from locations and weights.
                """)
public class SupplyRequestParams {
        // *************************************
        // create new view from locations and weights
        // *************************************
        @Schema(name = "supply_locations", description = """
                        Locations of supply points (in geographic coordinates).
                        """, example = "[[9.11, 51.23], [9.35, 50.98], [10.02, 52.10]]")
        @JsonProperty("supply_locations")
        public double[][] supply_locations;

        @Schema(name = "supply_weights", description = """
                        Weights of supply points (e.g. working times equivalent of doctors).
                        Weight for every point in 'supply_locations'.
                        If not provided, every supply point will be weighted with 1.
                        """, example = "[91, 34, 72]")
        @JsonProperty("supply_weights")
        public double[] supply_weights;
}
