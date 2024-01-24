package org.tud.oas.api.accessibility.de2sfca;

import org.tud.oas.requests.AccessResponseParams;
import org.tud.oas.requests.DecayRequestParams;
import org.tud.oas.requests.DemandRequestParams;
import org.tud.oas.requests.RoutingRequestParams;
import org.tud.oas.requests.SupplyRequestParams;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FCARequest", description = """
                FCA request.
                """)
class DE2SFCARequest {
        @JsonProperty("demand")
        public DemandRequestParams demand;

        @Schema(name = "decay_indices", description = """
                        Distance-decay for every demand-point (specified by index in "catchments")
                        """, example = "[0, 1, 0]")
        @JsonProperty("decay_indices")
        public int[] decay_indices;

        @JsonProperty("routing")
        public RoutingRequestParams routing;

        @JsonProperty("supply")
        public SupplyRequestParams supply;

        @JsonProperty("distance_decay")
        public DecayRequestParams[] distance_decays;

        @JsonProperty("response")
        public AccessResponseParams response_params;
}
