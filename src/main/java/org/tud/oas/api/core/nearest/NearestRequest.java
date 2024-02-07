package org.tud.oas.api.core.nearest;

import org.tud.oas.requests.DemandRequestParams;
import org.tud.oas.requests.RoutingRequestParams;
import org.tud.oas.requests.SupplyRequestParams;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NearestRequest", description = """
                Nearest request.
                """)
class NearestRequest {
        @JsonProperty("demand")
        public DemandRequestParams demand;

        @JsonProperty("routing")
        public RoutingRequestParams routing;

        @JsonProperty("supply")
        public SupplyRequestParams supply;

        @Schema(name = "range_max", description = """
                        Maximum range (for continuus range_type) in seconds.
                        """, example = "900")
        public Double range_max;
}
