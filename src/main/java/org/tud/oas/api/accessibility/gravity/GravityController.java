package org.tud.oas.api.accessibility.gravity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tud.oas.accessibility.Access;
import org.tud.oas.accessibility.GravityAccessibility;
import org.tud.oas.api.queries.aggregate.AggregateQueryController;
import org.tud.oas.api.responses.ErrorResponse;
import org.tud.oas.population.IPopulationView;
import org.tud.oas.population.PopulationManager;
import org.tud.oas.routing.RoutingManager;
import org.tud.oas.routing.IRoutingProvider;

@RestController
@RequestMapping("/v1/accessibility/gravity")
public class GravityController {
    private final Logger logger = LoggerFactory.getLogger(AggregateQueryController.class);

    /// <summary>
    /// Calculates simple gravity accessibility.
    /// </summary>
    @PostMapping
    public ResponseEntity<?> calcGravity(@RequestBody GravityRequest request) {
        IPopulationView view = PopulationManager.getPopulationView(request.population);
        if (view == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("accessibility/gravity",
                    "failed to get population-view, parameters are invalid"));
        }
        IRoutingProvider provider = RoutingManager.getRoutingProvider(request.routing);

        logger.debug("start calculation gravity accessibility");
        GravityAccessibility gravity = new GravityAccessibility();
        Access[] access = gravity.calcAccessibility(view, request.facility_locations, request.ranges,
                request.range_factors, provider);

        logger.debug("start building response");
        float[] response = this.buildResponse(view, access);
        logger.debug("response build successfully");

        return ResponseEntity.ok(new GravityResponse(response));
    }

    float[] buildResponse(IPopulationView population, Access[] accessibilities) {
        float[] response = new float[population.pointCount()];
        for (int i = 0; i < population.pointCount(); i++) {
            int index = i;
            float accessibility;
            if (accessibilities[index] != null) {
                accessibility = accessibilities[index].access;
            } else {
                accessibility = -9999;
            }
            response[i] = accessibility;
        }
        return response;
    }
}