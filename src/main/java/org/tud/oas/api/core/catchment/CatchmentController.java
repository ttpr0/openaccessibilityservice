package org.tud.oas.api.core.catchment;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.ICatchment;
import org.tud.oas.routing.RoutingOptions;
import org.tud.oas.services.DemandService;
import org.tud.oas.services.RoutingService;
import org.tud.oas.services.SupplyService;
import org.tud.oas.supply.ISupplyView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/v1/core/catchment")
public class CatchmentController {
    private final Logger logger = LoggerFactory.getLogger(CatchmentController.class);

    private RoutingService routing_service;
    private DemandService demand_service;
    private SupplyService supply_service;

    @Autowired
    public CatchmentController(RoutingService routing, DemandService demand, SupplyService supply) {
        this.routing_service = routing;
        this.demand_service = demand;
        this.supply_service = supply;
    }

    @Operation(description = """
            Calculates simple floating catchment area.
            """)
    @ApiResponse(responseCode = "200", description = "Standard response for successfully processed requests.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = CatchmentResponse.class))
    })
    @PostMapping
    public CatchmentResponse calcFCA(@RequestBody CatchmentRequest request) {
        logger.info("Run Nearest Request");

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
        IRoutingProvider provider = routing_service.getRoutingProvider(request.routing);

        RoutingOptions options = new RoutingOptions("isochrones", request.range_max);

        ICatchment table = provider.requestCatchment(demand_view, supply_view, request.range_max, options);

        List<Integer>[] catchment = new List[demand_view.pointCount()];
        for (int i = 0; i < demand_view.pointCount(); i++) {
            List<Integer> cat = new ArrayList<Integer>();
            for (int neighbour : table.getNeighbours(i)) {
                cat.add(neighbour);
            }
            catchment[i] = cat;
        }

        return new CatchmentResponse(catchment);
    }
}
