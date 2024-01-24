package org.tud.oas.api.accessibility.e3sfca;

import org.tud.oas.requests.AccessResponseParams;
import org.tud.oas.requests.DecayRequestParams;
import org.tud.oas.requests.DemandRequestParams;
import org.tud.oas.requests.RoutingRequestParams;
import org.tud.oas.requests.SupplyRequestParams;
import org.locationtech.jts.geom.Coordinate;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FCARequest", description = """
                FCA request.
                """)
class E3SFCARequest {
        @JsonProperty("demand")
        public DemandRequestParams demand;

        @JsonProperty("distance_decay")
        public DecayRequestParams distance_decay;

        @JsonProperty("routing")
        public RoutingRequestParams routing;

        @JsonProperty("supply")
        public SupplyRequestParams supply;

        @Schema(name = "supply_attraction", description = """
                        Weight of supply while computing selection propabilities.
                        Defaults to all ones.
                        """, example = "[1, 2, 1.3]")
        @JsonProperty("supply_attraction")
        public float[] supply_attraction;

        @JsonProperty("response")
        public AccessResponseParams response_params;
}
