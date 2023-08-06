package org.tud.oas.api.fca;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tud.oas.accessibility.Enhanced2SFCA;
import org.tud.oas.accessibility.distance_decay.DistanceDecay;
import org.tud.oas.accessibility.distance_decay.IDistanceDecay;
import org.tud.oas.api.responses.ErrorResponse;
import org.tud.oas.population.IPopulationView;
import org.tud.oas.population.PopulationManager;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.RoutingManager;

@RestController
@RequestMapping("/v1/fca")
public class FCAController {

    @PostMapping
    public ResponseEntity<?> calcFCA(@RequestBody FCARequest request) {
        IPopulationView view = PopulationManager.getPopulationView(request.population);
        if (view == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("2sfca/enhanced", "failed to get population-view, parameters are invalid"));
        }
        IRoutingProvider provider = RoutingManager.getRoutingProvider(request.routing);
        IDistanceDecay decay = DistanceDecay.getDistanceDecay(request.distance_decay);
        if (decay == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("2sfca/enhanced", "failed to get distance-decay, parameters are invalid"));
        }
        if (request.facility_locations == null || request.facility_capacities == null
                || request.ranges == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("2sfca/enhanced",
                    "facility or range parameters missing, parameters are invalid"));
        }

        float[] weights = Enhanced2SFCA.calc2SFCA(view, request.facility_locations, request.facility_capacities,
                request.ranges, decay, provider, request.mode);

        float maxWeight = 0;
        for (float w : weights) {
            if (w > maxWeight) {
                maxWeight = w;
            }
        }
        float factor = 100 / maxWeight;

        float[] response = buildResponse(view, weights, factor);
        return ResponseEntity.ok(new FCAResponse(response));
    }

    private float[] buildResponse(IPopulationView population, float[] accessibilities, float factor) {
        for (int i = 0; i < accessibilities.length; i++) {
            float accessibility = accessibilities[i];
            if (accessibility != 0) {
                accessibility = accessibility * factor;
            } else {
                accessibility = -9999;
            }
            accessibilities[i] = accessibility;
        }
        return accessibilities;
    }
}
