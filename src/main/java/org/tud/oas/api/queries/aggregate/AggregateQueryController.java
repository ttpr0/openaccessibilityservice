package org.tud.oas.api.queries.aggregate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tud.oas.api.responses.ErrorResponse;
import org.tud.oas.population.IPopulationView;
import org.tud.oas.population.PopulationManager;
import org.tud.oas.routing.ICatchment;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.RoutingManager;

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
        IPopulationView view;

        if (request.session_id != null) {
            UUID sessionId = request.session_id;
            if (!sessions.containsKey(sessionId)) {
                return ResponseEntity.badRequest().body(new ErrorResponse("n_nearest", "no active session found"));
            }
            AggregateQuerySession session = sessions.get(sessionId);
            catchment = session.catchment;
            view = session.population_view;
        } else {
            view = PopulationManager.getPopulationView(request.population);
            if (view == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("queries/aggregate",
                        "failed to get population-view, parameters are invalid"));
            }

            IRoutingProvider provider = RoutingManager.getRoutingProvider(request.routing);
            catchment = provider.requestCatchment(view, request.facility_locations, request.range,
                    "isochrones");
        }

        UUID id = UUID.randomUUID();

        sessions.put(id, new AggregateQuerySession(id, view, catchment));

        float[] results = AggregateQuery.computeQuery(request.facility_values, catchment, request.compute_type);

        return ResponseEntity.ok(new AggregateQueryResponse(results, id));
    }
}
