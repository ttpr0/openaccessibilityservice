package org.tud.oas.api.queries.aggregate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tud.oas.api.responses.ErrorResponse;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.demand.DemandManager;
import org.tud.oas.routing.ICatchment;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.RoutingManager;
import org.tud.oas.supply.ISupplyView;
import org.tud.oas.supply.SupplyManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/queries/aggregate")
public class AggregateQueryController {

    static Map<UUID, AggregateQuerySession> sessions = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(AggregateQueryController.class);

    /// <summary>
    /// Calculates aggregate query.
    /// </summary>
    @PostMapping
    public ResponseEntity<?> calcQuery(@RequestBody AggregateQueryRequest request) {
        ICatchment catchment;
        IDemandView demand_view;
        ISupplyView supply_view = SupplyManager.getSupplyView(request.supply);
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
            demand_view = DemandManager.getDemandView(request.demand);
            if (demand_view == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("queries/aggregate",
                        "failed to get population-view, parameters are invalid"));
            }

            IRoutingProvider provider = RoutingManager.getRoutingProvider(request.routing);
            catchment = provider.requestCatchment(demand_view, supply_view, request.range,
                    "isochrones");
        }

        UUID id = UUID.randomUUID();

        sessions.put(id, new AggregateQuerySession(id, demand_view, catchment));

        float[] results = AggregateQuery.computeQuery(supply_view, catchment, request.compute_type);

        return ResponseEntity.ok(new AggregateQueryResponse(results, id));
    }
}
