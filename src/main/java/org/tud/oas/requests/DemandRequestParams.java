package org.tud.oas.requests;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DemandRequestParams", description = """
                Demand Request Parameters.
                Depending on which parameters are not null demand view will be created differently.
                If "view_id" is not null => stored view will be used.
                If "demand_locations" and "demand_weights" not null => locations and weights will be used.
                Else => envelope and internaly stored population dataset will be used.
                """)
public class DemandRequestParams {
        // *************************************
        // use stored view
        // *************************************
        @Schema(name = "view_id", description = """
                        ID of stored demand view.
                        """)
        @JsonProperty("demand_id")
        public UUID view_id;

        // *************************************
        // create new view from locations and weights
        // *************************************
        @Schema(name = "demand_locations", description = """
                        Locations of demand points (in geographic coordinates).
                        """, example = "[[9.11, 51.23], [9.35, 50.98], [10.02, 52.10]]")
        @JsonProperty("demand_locations")
        public double[][] demand_locations;

        @Schema(name = "demand_weights", description = """
                        Weights of demand points (e.g. population count).
                        Weight for every point in 'demand_locations'.
                        """, example = "[91, 34, 72]")
        @JsonProperty("demand_weights")
        public double[] demand_weights;

        // **************************************
        // create new view from internal population data
        // **************************************
        @Schema(name = "population_name", description = """
                        Name of the internally stored population data.
                        """, example = "default_population")
        @JsonProperty("population_name")
        public String population_name;

        @Schema(name = "envelop", description = """
                        Envelope for which population data should be used.
                        [minx, miny, maxx, maxy]
                        """, example = "[9.11, 50.98, 10.23, 52.09]")
        @JsonProperty("envelop")
        public double[] envelop;

        @Schema(name = "area", description = """
                        Polygon-area for which population data should be used (no-holes).
                        """, example = "[[1, 1], [10, 1], [10, 10], [1, 10]]")
        @JsonProperty("area")
        public double[][] area;

        @Schema(name = "population_indizes", description = """
                        Gives the indizes of population data to be included in population count.
                        E.g. for DVAN-Data: 20-39 and 40-59 => [2, 3]
                        """, example = "[2, 3]")
        @JsonProperty("population_indizes")
        public int[] population_indizes;

        @Schema(name = "population_factors", description = """
                        Scaling factor for every given index of population data.
                        """, example = "[1.0, 0.3]")
        @JsonProperty("population_factors")
        public float[] population_factors;
}
