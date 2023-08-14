package org.tud.oas.api.accessibility.simple;

import java.util.List;

import org.tud.oas.demand.DemandRequestParams;
import org.tud.oas.routing.RoutingRequestParams;
import org.tud.oas.supply.SupplyRequestParams;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SimpleAccessibilityRequest", description = """
        Simple-accessibility Request.
        """)
public class SimpleAccessibilityRequest {
    @Schema(name = "ranges", description = """
            Ranges (in sec) to be used by calculation.
            """, example = "[180, 360, 540, 720, 900]")
    public List<Double> ranges;

    public SupplyRequestParams supply;

    public DemandRequestParams demand;

    public RoutingRequestParams routing;
}