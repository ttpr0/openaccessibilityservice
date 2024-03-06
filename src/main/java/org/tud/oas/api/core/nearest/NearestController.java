package org.tud.oas.api.core.nearest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.INNTable;
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
@RequestMapping("/v1/core/nearest")
public class NearestController {
    private final Logger logger = LoggerFactory.getLogger(NearestController.class);

    private RoutingService routing_service;
    private DemandService demand_service;
    private SupplyService supply_service;

    @Autowired
    public NearestController(RoutingService routing, DemandService demand, SupplyService supply) {
        this.routing_service = routing;
        this.demand_service = demand;
        this.supply_service = supply;
    }

    @Operation(description = """
            Calculates simple floating catchment area.
            """)
    @ApiResponse(responseCode = "200", description = "Standard response for successfully processed requests.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = NearestResponse.class))
    })
    @PostMapping
    public NearestResponse calcFCA(@RequestBody NearestRequest request) {
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

        RoutingOptions options = new RoutingOptions("matrix", request.range_max);

        INNTable table;
        try {
            table = provider.requestNearest(demand_view, supply_view, options);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "failed to compute nearest-neighbours: " + e.getMessage());
        }

        NearestItem[] nearest = new NearestItem[demand_view.pointCount()];
        for (int i = 0; i < demand_view.pointCount(); i++) {
            int id = table.getNearest(i);
            float range = table.getNearestRange(i);
            nearest[i] = new NearestItem(id, range);
        }

        return new NearestResponse(nearest);
    }
}
