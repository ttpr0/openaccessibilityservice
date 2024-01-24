package org.tud.oas.api.accessibility.opportunity;

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
public class OpportunityRequest {
    @JsonProperty("demand")
    public DemandRequestParams demand;

    @JsonProperty("supply")
    public SupplyRequestParams supply;

    @JsonProperty("decay")
    public DecayRequestParams distance_decay;

    @JsonProperty("routing")
    public RoutingRequestParams routing;

    @JsonProperty("response")
    public AccessResponseParams response_params;
}
