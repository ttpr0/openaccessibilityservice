package org.tud.oas.api.accessibility.e2sfca;

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
class E2SFCARequest {
        @JsonProperty("demand")
        public DemandRequestParams demand;

        @JsonProperty("distance_decay")
        public DecayRequestParams distance_decay;

        @JsonProperty("routing")
        public RoutingRequestParams routing;

        @JsonProperty("supply")
        public SupplyRequestParams supply;

        @JsonProperty("response")
        public AccessResponseParams response_params;
}
