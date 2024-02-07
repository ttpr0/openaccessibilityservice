package org.tud.oas.api.core.k_nearest;

import org.tud.oas.requests.DemandRequestParams;
import org.tud.oas.requests.RoutingRequestParams;
import org.tud.oas.requests.SupplyRequestParams;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "KNearestRequest", description = """
                KNearest request.
                """)
class KNearestRequest {
        @JsonProperty("demand")
        public DemandRequestParams demand;

        @JsonProperty("routing")
        public RoutingRequestParams routing;

        @JsonProperty("supply")
        public SupplyRequestParams supply;

        @Schema(name = "count", description = """
                        Number of nearest neighbours to find.
                        """, example = "3")
        public Integer count;

        @Schema(name = "range_max", description = """
                        Maximum range (for continuus range_type) in seconds.
                        """, example = "900")
        public Double range_max;
}
