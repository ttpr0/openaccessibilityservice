package org.tud.oas.api.queries.n_nearest;

import org.tud.oas.demand.IDemandView;
import org.tud.oas.responses.ErrorResponse;
import org.tud.oas.routing.IKNNTable;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/v1/queries/n_nearest")
public class NNearestQueryController {
    static Map<UUID, NNearestQuerySession> sessions = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(NNearestQueryController.class);

    private RoutingService routing_service;
    private DemandService demand_service;
    private SupplyService supply_service;

    @Autowired
    public NNearestQueryController(RoutingService routing, DemandService demand, SupplyService supply) {
        this.routing_service = routing;
        this.demand_service = demand;
        this.supply_service = supply;
    }

    @Operation(description = """
            Calculates nnearest query.
            """)
    @ApiResponse(responseCode = "200", description = "Standard response for successfully processed requests.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = NNearestQueryResponse.class))
    })
    @ApiResponse(responseCode = "400", description = "The request is incorrect and therefore can not be processed.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    })
    @PostMapping
    public ResponseEntity<?> calcQuery(@RequestBody NNearestQueryRequest request) {
        IKNNTable table;
        IDemandView demand_view;
        UUID sessionId;
        ISupplyView supply_view = supply_service.getSupplyView(request.supply);
        if (supply_view == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("queries/n_nearest",
                    "failed to get supply-view, parameters are invalid"));
        }

        if (request.session_id != null) {
            sessionId = request.session_id;
            if (!sessions.containsKey(sessionId)) {
                return ResponseEntity.badRequest().body(new ErrorResponse("n_nearest", "no active session found"));
            }
            NNearestQuerySession session = sessions.get(sessionId);
            table = session.table;
            demand_view = session.demand_view;
        } else {
            demand_view = demand_service.getDemandView(request.demand);
            if (demand_view == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("accessibility/gravity/grid",
                        "failed to get population-view, parameters are invalid"));
            }

            IRoutingProvider provider = routing_service.getRoutingProvider(request.routing);
            table = provider.requestKNearest(demand_view, supply_view, request.facility_count,
                    new RoutingOptions("isochrones", request.ranges));

            sessionId = UUID.randomUUID();
            sessions.put(sessionId, new NNearestQuerySession(sessionId, demand_view, table));
        }

        float[] results = NNearestQuery.computeQuery(supply_view, table, request.compute_type,
                request.facility_count);

        return ResponseEntity.ok(new NNearestQueryResponse(results, sessionId));
    }
}
