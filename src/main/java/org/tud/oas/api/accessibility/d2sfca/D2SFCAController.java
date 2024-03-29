package org.tud.oas.api.accessibility.d2sfca;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.tud.oas.accessibility.fca.Dynamic2SFCA;
import org.tud.oas.accessibility.fca.Enhanced2SFCA;
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
@RequestMapping("/v1/accessibility/dynamic_2sfca")
public class D2SFCAController {
    private final Logger logger = LoggerFactory.getLogger(D2SFCAController.class);

    private RoutingService routing_service;
    private DemandService demand_service;
    private SupplyService supply_service;
    private DecayService decay_service;

    @Autowired
    public D2SFCAController(RoutingService routing, DemandService demand, SupplyService supply, DecayService decay) {
        this.routing_service = routing;
        this.demand_service = demand;
        this.supply_service = supply;
        this.decay_service = decay;
    }

    @Operation(description = """
            Calculates dynamic two-step-floating-catchment-area.
            """)
    @ApiResponse(responseCode = "200", description = "Standard response for successfully processed requests.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = AccessResponse.class))
    })
    @PostMapping
    public AccessResponse calcFCA(@RequestBody D2SFCARequest request) {
        logger.info("Run FCA Request");

        // get parameters from request
        IDemandView demand_view = demand_service.getDemandView(request.demand);
        if (demand_view == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "failed to get demand-view, parameters are invalid");
        }
        int[] catchment_indices = request.catchment_indices;
        if (catchment_indices == null || catchment_indices.length != demand_view.pointCount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "failed to retrive catchment indizes, parameter is invalid");
        }
        ISupplyView supply_view = supply_service.getSupplyView(request.supply);
        if (supply_view == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "failed to get supply-view, parameters are invalid");
        }
        IRoutingProvider provider = routing_service.getRoutingProvider(request.routing);
        float[] catchments = request.catchments;
        if (catchments == null || catchments.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "failed to retrive catchments, parameter is invalid");
        }
        List<Double> ranges = new ArrayList(catchments.length);
        for (int i = 0; i < catchments.length; i++) {
            ranges.add((double) catchments[i]);
        }
        RoutingOptions options = new RoutingOptions("isochrones", ranges);
        if (!AccessResponse.checkParams(request.response_params)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "response parameters are invalid");
        }

        try {
            // compute accessibility result
            float[] weights = Dynamic2SFCA.calc2SFCA(demand_view, supply_view, ranges, catchment_indices, provider,
                    options);

            // build response
            return new AccessResponse(weights, demand_view, request.response_params);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
