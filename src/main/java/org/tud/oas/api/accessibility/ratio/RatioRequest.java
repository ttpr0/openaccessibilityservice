package org.tud.oas.api.accessibility.ratio;

import java.util.List;

import org.tud.oas.requests.AccessResponseParams;
import org.tud.oas.requests.DecayRequestParams;
import org.tud.oas.requests.DemandRequestParams;
import org.tud.oas.requests.RoutingRequestParams;
import org.tud.oas.requests.SupplyRequestParams;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ReachabilityRequest", description = """
        Reachability request.
        """)
public class RatioRequest {
    @JsonProperty("demand")
    public DemandRequestParams demand;

    @JsonProperty("supply")
    public SupplyRequestParams supply;

    @Schema(name = "catchment", description = """
            Travel threshold (depending on routing mode in seconds or meters).
            """, example = "900")
    @JsonProperty("catchment")
    public Float catchment;

    @JsonProperty("routing")
    public RoutingRequestParams routing;

    @JsonProperty("response")
    public AccessResponseParams response_params;
}
