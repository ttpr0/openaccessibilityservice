package org.tud.oas.api.accessibility.nn2sfca;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.tud.oas.accessibility.distance_decay.IDistanceDecay;
import org.tud.oas.accessibility.fca.NNM2SFCA;
import org.tud.oas.api.accessibility.AccessResponse;
import org.tud.oas.demand.IDemandView;
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
@RequestMapping("/v1/accessibility/nearest_neighbour_2sfca")
public class NN2SFCAController {
    private final Logger logger = LoggerFactory.getLogger(NN2SFCAController.class);

    private RoutingService routing_service;
    private DemandService demand_service;
    private SupplyService supply_service;
    private DecayService decay_service;

    @Autowired
    public NN2SFCAController(RoutingService routing, DemandService demand, SupplyService supply, DecayService decay) {
        this.routing_service = routing;
        this.demand_service = demand;
        this.supply_service = supply;
        this.decay_service = decay;
    }

    @Operation(description = """
            Calculates nearest-neighbour modified two-step-floating-catchment-area.
            """)
    @ApiResponse(responseCode = "200", description = "Standard response for successfully processed requests.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = AccessResponse.class))
    })
    @PostMapping
    public AccessResponse calcFCA(@RequestBody NN2SFCARequest request) {
        logger.info("Run FCA Request");

        // get parameters from request
        IDemandView demand_view = demand_service.getDemandView(request.demand);
        if (demand_view == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "failed to get demand-view, parameters are invalid");
        }
        ISupplyView supply_view = supply_service.getSupplyView(request.supply);
        if (supply_view == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "failed to get supply-view, parameters are invalid");
        }
        Integer count = request.nearest_count;
        if (count == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "failed to retrive nearest_count, parameter is invalid");
        }
        IRoutingProvider provider = routing_service.getRoutingProvider(request.routing);
        IDistanceDecay decay = decay_service.getDistanceDecay(request.distance_decay);
        if (decay == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "failed to get distance-decay, parameters are invalid");
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
        float[] weights = NNM2SFCA.calc2SFCA(demand_view, supply_view, count, decay, provider, options);

        // build response
        return new AccessResponse(weights, demand_view, request.response_params);
    }
}
