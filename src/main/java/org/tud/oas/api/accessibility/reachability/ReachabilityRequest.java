package org.tud.oas.api.accessibility.reachability;

import java.util.List;

import org.tud.oas.accessibility.distance_decay.DecayRequestParams;
import org.tud.oas.demand.DemandRequestParams;
import org.tud.oas.routing.RoutingRequestParams;
import org.tud.oas.supply.SupplyRequestParams;

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
