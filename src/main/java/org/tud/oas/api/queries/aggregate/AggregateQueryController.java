package org.tud.oas.api.queries.aggregate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.responses.ErrorResponse;
import org.tud.oas.routing.ICatchment;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/queries/aggregate")
public class AggregateQueryController {

    static Map<UUID, AggregateQuerySession> sessions = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(AggregateQueryController.class);

    private RoutingService routing_service;
    private DemandService demand_service;
    private SupplyService supply_service;

    @Autowired
    public AggregateQueryController(RoutingService routing, DemandService demand, SupplyService supply) {
        this.routing_service = routing;
        this.demand_service = demand;
        this.supply_service = supply;
    }

    @Operation(description = """
            Calculates aggregate query.
            """)
    @ApiResponse(responseCode = "200", description = "Standard response for successfully processed requests.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = AggregateQueryResponse.class))
    })
    @ApiResponse(responseCode = "400", description = "The request is incorrect and therefore can not be processed.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    })
    @PostMapping
    public ResponseEntity<?> calcQuery(@RequestBody AggregateQueryRequest request) {
        ICatchment catchment;
        IDemandView demand_view;
        ISupplyView supply_view = supply_service.getSupplyView(request.supply);
        if (supply_view == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("queries/aggregate",
                    "failed to get supply-view, parameters are invalid"));
        }

        if (request.session_id != null) {
            UUID sessionId = request.session_id;
            if (!sessions.containsKey(sessionId)) {
                return ResponseEntity.badRequest().body(new ErrorResponse("n_nearest", "no active session found"));
            }
            AggregateQuerySession session = sessions.get(sessionId);
            catchment = session.catchment;
            demand_view = session.demand_view;
        } else {
            demand_view = demand_service.getDemandView(request.demand);
            if (demand_view == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("queries/aggregate",
                        "failed to get population-view, parameters are invalid"));
            }

            IRoutingProvider provider = routing_service.getRoutingProvider(request.routing);
            catchment = provider.requestCatchment(demand_view, supply_view, request.range,
                    new RoutingOptions("isochrones"));
        }

        UUID id = UUID.randomUUID();

        sessions.put(id, new AggregateQuerySession(id, demand_view, catchment));

        float[] results = AggregateQuery.computeQuery(supply_view, catchment, request.compute_type);

        return ResponseEntity.ok(new AggregateQueryResponse(results, id));
    }
}
