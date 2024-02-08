package org.tud.oas.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DemandRequestParams", description = """
        Demand Request Parameters.
        Depending on which parameters are not null demand view will be created differently.
        If "demand_locations" and "demand_weights" not null => locations and weights will be used.
        """)
public class DemandRequestParams {
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

    @Schema(name = "loc_crs", description = """
            Optionally specifies the CRS of the locations.
            If not specified locations are already expected to be geographic.
            """, example = "EPSG:28532")
    @JsonProperty("loc_crs")
    public String loc_crs;
}
