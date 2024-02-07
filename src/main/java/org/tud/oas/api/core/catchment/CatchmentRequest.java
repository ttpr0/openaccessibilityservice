package org.tud.oas.api.core.catchment;

import org.tud.oas.requests.AccessResponseParams;
import org.tud.oas.requests.DecayRequestParams;
import org.tud.oas.requests.DemandRequestParams;
import org.tud.oas.requests.RoutingRequestParams;
import org.tud.oas.requests.SupplyRequestParams;
import org.locationtech.jts.geom.Coordinate;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CatchmentRequest", description = """
                Catchment request.
                """)
class CatchmentRequest {
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
