package org.tud.oas.api.fca;

import org.tud.oas.accessibility.distance_decay.DecayRequestParams;
import org.tud.oas.demand.DemandRequestParams;
import org.tud.oas.routing.RoutingRequestParams;
import org.tud.oas.supply.SupplyRequestParams;

import org.locationtech.jts.geom.Coordinate;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FCARequest", description = """
        FCA request.
        """)
class FCARequest {
    @JsonProperty("demand")
    public DemandRequestParams demand;

    @JsonProperty("distance_decay")
    public DecayRequestParams distance_decay;

    @JsonProperty("routing")
    public RoutingRequestParams routing;

    @JsonProperty("supply")
    public SupplyRequestParams supply;

    @Schema(name = "mode", description = """
            Calculation mode (one of "isochrones", "isoraster", "matrix").
            Defaults to "isochrones".
            """, example = "isochrones")
    @JsonProperty("mode")
    public String mode;
}
