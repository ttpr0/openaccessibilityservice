package org.tud.oas.api.accessibility.m2sfca;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tud.oas.accessibility.distance_decay.IDistanceDecay;
import org.tud.oas.accessibility.fca.Enhanced2SFCA;
import org.tud.oas.accessibility.fca.Modified2SFCA;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.responses.AccessResponse;
import org.tud.oas.responses.ErrorResponse;
import org.tud.oas.routing.IRoutingProvider;
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

@RestController
@RequestMapping("/v1/accessibility/modified_2sfca")
public class M2SFCAController {
    private final Logger logger = LoggerFactory.getLogger(M2SFCAController.class);

    private RoutingService routing_service;
    private DemandService demand_service;
    private SupplyService supply_service;
    private DecayService decay_service;

    @Autowired
    public M2SFCAController(RoutingService routing, DemandService demand, SupplyService supply, DecayService decay) {
        this.routing_service = routing;
        this.demand_service = demand;
        this.supply_service = supply;
        this.decay_service = decay;
    }

    @Operation(description = """
            Calculates modified two-step-floating-catchment-area.
            """)
    @ApiResponse(responseCode = "200", description = "Standard response for successfully processed requests.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = AccessResponse.class))
    })
    @ApiResponse(responseCode = "400", description = "The request is incorrect and therefore can not be processed.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    })
    @PostMapping
    public ResponseEntity<?> calcFCA(@RequestBody M2SFCARequest request) {
        logger.info("Run FCA Request");

        // get parameters from request
        IDemandView demand_view = demand_service.getDemandView(request.demand);
        if (demand_view == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("2sfca/enhanced", "failed to get demand-view, parameters are invalid"));
        }
        ISupplyView supply_view = supply_service.getSupplyView(request.supply);
        if (supply_view == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("2sfca/enhanced",
                    "failed to get supply-view, parameters are invalid"));
        }
        IRoutingProvider provider = routing_service.getRoutingProvider(request.routing);
        IDistanceDecay decay = decay_service.getDistanceDecay(request.distance_decay);
        if (decay == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("2sfca/enhanced", "failed to get distance-decay, parameters are invalid"));
        }
        RoutingOptions options;
        if (decay.getDistances() == null) {
            options = new RoutingOptions("matrix", (double) decay.getMaxDistance());
        } else {
            float[] distances = decay.getDistances();
            List<Double> ranges = new ArrayList(distances.length);
            for (int i = 0; i < distances.length; i++) {
                ranges.add((double) distances[i]);
            }
            options = new RoutingOptions("isochrones", ranges);
        }

        // compute accessibility result
        float[] weights = Modified2SFCA.calc2SFCA(demand_view, supply_view, decay, provider, options);

        // build response
        return ResponseEntity.ok(new AccessResponse(weights, demand_view, request.response_params));
    }
}
