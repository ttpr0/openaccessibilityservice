package org.tud.oas.api.accessibility.de2sfca;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tud.oas.accessibility.distance_decay.IDistanceDecay;
import org.tud.oas.accessibility.fca.DE2SFCA;
import org.tud.oas.accessibility.fca.Dynamic2SFCA;
import org.tud.oas.accessibility.fca.Enhanced2SFCA;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.requests.DecayRequestParams;
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
@RequestMapping("/v1/accessibility/dynamic_enhanced_2sfca")
public class DE2SFCAController {
    private final Logger logger = LoggerFactory.getLogger(DE2SFCAController.class);

    private RoutingService routing_service;
    private DemandService demand_service;
    private SupplyService supply_service;
    private DecayService decay_service;

    @Autowired
    public DE2SFCAController(RoutingService routing, DemandService demand, SupplyService supply, DecayService decay) {
        this.routing_service = routing;
        this.demand_service = demand;
        this.supply_service = supply;
        this.decay_service = decay;
    }

    @Operation(description = """
            Calculates dynamic enhanced two-step-floating-catchment-area.
            """)
    @ApiResponse(responseCode = "200", description = "Standard response for successfully processed requests.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = AccessResponse.class))
    })
    @ApiResponse(responseCode = "400", description = "The request is incorrect and therefore can not be processed.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    })
    @PostMapping
    public ResponseEntity<?> calcFCA(@RequestBody DE2SFCARequest request) {
        logger.info("Run FCA Request");

        // get parameters from request
        IDemandView demand_view = demand_service.getDemandView(request.demand);
        if (demand_view == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("2sfca/enhanced", "failed to get demand-view, parameters are invalid"));
        }
        int[] decay_indices = request.decay_indices;
        if (decay_indices == null || decay_indices.length != demand_view.pointCount()) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("2sfca/enhanced", "failed to retrive catchment indizes, parameter is invalid"));
        }
        ISupplyView supply_view = supply_service.getSupplyView(request.supply);
        if (supply_view == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("2sfca/enhanced",
                    "failed to get supply-view, parameters are invalid"));
        }
        IRoutingProvider provider = routing_service.getRoutingProvider(request.routing);
        DecayRequestParams[] catchments = request.distance_decays;
        if (catchments == null || catchments.length == 0) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("2sfca/enhanced", "failed to retrive decayss, parameter is invalid"));
        }
        List<IDistanceDecay> decays = new ArrayList(catchments.length);
        Double max_range = 0.0;
        for (int i = 0; i < catchments.length; i++) {
            DecayRequestParams params = catchments[i];
            IDistanceDecay decay = decay_service.getDistanceDecay(params);
            if (decay == null) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("2sfca/enhanced",
                                "failed to get distance-decay " + i + 1 + ", parameters are invalid"));
            }
            decays.add(decay);
            if (decay.getMaxDistance() > max_range) {
                max_range = (double) decay.getMaxDistance();
            }
        }
        RoutingOptions options = new RoutingOptions("matrix", max_range);

        // compute accessibility result
        float[] weights = DE2SFCA.calc2SFCA(demand_view, supply_view, decays, decay_indices, provider,
                options);

        // build response
        return ResponseEntity.ok(new AccessResponse(weights, demand_view, request.response_params));
    }
}
