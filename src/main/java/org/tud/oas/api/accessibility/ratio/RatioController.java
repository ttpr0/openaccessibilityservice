package org.tud.oas.api.accessibility.ratio;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.tud.oas.accessibility.SimpleReachability;
import org.tud.oas.accessibility.SimpleSupplyDemandRatio;
import org.tud.oas.accessibility.distance_decay.IDistanceDecay;
import org.tud.oas.api.queries.aggregate.AggregateQueryController;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.responses.AccessResponse;
import org.tud.oas.responses.ErrorResponse;
import org.tud.oas.routing.RoutingOptions;
import org.tud.oas.services.DecayService;
import org.tud.oas.services.DemandService;
import org.tud.oas.services.RoutingService;
import org.tud.oas.services.SupplyService;
import org.tud.oas.supply.ISupplyView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.tud.oas.routing.IRoutingProvider;

@RestController
@RequestMapping("/v1/accessibility/supply_demand_ratio")
public class RatioController {
    private final Logger logger = LoggerFactory.getLogger(AggregateQueryController.class);

    private RoutingService routing_service;
    private DemandService demand_service;
    private SupplyService supply_service;
    private DecayService decay_service;

    @Autowired
    public RatioController(RoutingService routing, DemandService demand, SupplyService supply,
            DecayService decay) {
        this.routing_service = routing;
        this.demand_service = demand;
        this.supply_service = supply;
        this.decay_service = decay;
    }

    @Operation(description = """
            Calculates simple reachability.
            """)
    @ApiResponse(responseCode = "200", description = "Standard response for successfully processed requests.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = AccessResponse.class))
    })
    @ApiResponse(responseCode = "400", description = "The request is incorrect and therefore can not be processed.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    })
    @PostMapping
    public ResponseEntity<?> calcReachability(@RequestBody RatioRequest request) {
        IDemandView demand_view = demand_service.getDemandView(request.demand);
        if (demand_view == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("accessibility/gravity",
                    "failed to get demand-view, parameters are invalid"));
        }
        ISupplyView supply_view = supply_service.getSupplyView(request.supply);
        if (supply_view == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("accessibility/gravity",
                    "failed to get supply-view, parameters are invalid"));
        }
        IRoutingProvider provider = routing_service.getRoutingProvider(request.routing);
        Float catchment = request.catchment;
        if (catchment == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("accessibility/2sfca",
                            "failed to get catchment size, parameter is missing"));
        }
        RoutingOptions options = new RoutingOptions("isochrones", (double) catchment);

        logger.debug("start calculation gravity accessibility");
        float[] access = SimpleSupplyDemandRatio.calcAccessibility(demand_view, supply_view, catchment, provider,
                options);

        logger.debug("start building response");
        return ResponseEntity.ok(new AccessResponse(access, demand_view, request.response_params));
    }
}