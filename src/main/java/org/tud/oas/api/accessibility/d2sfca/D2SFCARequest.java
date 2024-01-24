package org.tud.oas.api.accessibility.d2sfca;

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
class D2SFCARequest {
        @JsonProperty("demand")
        public DemandRequestParams demand;

        @Schema(name = "catchment_indices", description = """
                        Catchment for every demand-point (specified by index in "catchments")
                        """, example = "[0, 1, 0]")
        @JsonProperty("catchment_indices")
        public int[] catchment_indices;

        @JsonProperty("routing")
        public RoutingRequestParams routing;

        @JsonProperty("supply")
        public SupplyRequestParams supply;

        @Schema(name = "catchments", description = """
                        Travel thresholds (depending on routing mode in seconds or meters).
                        """, example = "[900, 1200, 1500]")
        @JsonProperty("catchments")
        public float[] catchments;

        @JsonProperty("response")
        public AccessResponseParams response_params;
}
