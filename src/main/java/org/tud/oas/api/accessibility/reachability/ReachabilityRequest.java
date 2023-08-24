package org.tud.oas.api.accessibility.reachability;

import java.util.List;

import org.tud.oas.requests.DecayRequestParams;
import org.tud.oas.requests.DemandRequestParams;
import org.tud.oas.requests.RoutingRequestParams;
import org.tud.oas.requests.SupplyRequestParams;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ReachabilityRequest", description = """
        Reachability request.
        """)
public class ReachabilityRequest {
    public DecayRequestParams distance_decay;

    public DemandRequestParams demand;

    public SupplyRequestParams supply;

    public RoutingRequestParams routing;
}
