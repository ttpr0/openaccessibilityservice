package org.tud.oas.api.queries.n_nearest;

import org.tud.oas.api.responses.ErrorResponse;
import org.tud.oas.population.IPopulationView;
import org.tud.oas.population.PopulationManager;
import org.tud.oas.routing.IKNNTable;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.RoutingManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    /// <summary>
    /// Calculates nnearest query.
    /// </summary>
    @PostMapping
    public ResponseEntity<?> calcQuery(@RequestBody NNearestQueryRequest request) {
        IKNNTable table;
        IPopulationView view;
        UUID sessionId;

        if (request.session_id != null) {
            sessionId = request.session_id;
            if (!sessions.containsKey(sessionId)) {
                return ResponseEntity.badRequest().body(new ErrorResponse("n_nearest", "no active session found"));
            }
            NNearestQuerySession session = sessions.get(sessionId);
            table = session.table;
            view = session.population_view;
        } else {
            view = PopulationManager.getPopulationView(request.population);
            if (view == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("accessibility/gravity/grid",
                        "failed to get population-view, parameters are invalid"));
            }

            IRoutingProvider provider = RoutingManager.getRoutingProvider(request.routing);
            table = provider.requestKNearest(view, request.facility_locations, request.ranges,
                    request.facility_count, "isochrones");

            sessionId = UUID.randomUUID();
            sessions.put(sessionId, new NNearestQuerySession(sessionId, view, table));
        }

        float[] results = NNearestQuery.computeQuery(request.facility_values, table, request.compute_type,
                request.facility_count);

        return ResponseEntity.ok(new NNearestQueryResponse(results, sessionId));
    }
}
