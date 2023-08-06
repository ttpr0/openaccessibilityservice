package org.tud.oas.api.accessibility.multi_criteria;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tud.oas.accessibility.GravityAccessibility;
import org.tud.oas.accessibility.MultiCriteraAccessibility;
import org.tud.oas.api.queries.aggregate.AggregateQueryController;
import org.tud.oas.api.responses.ErrorResponse;
import org.tud.oas.population.IPopulationView;
import org.tud.oas.population.PopulationManager;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.RoutingManager;

@RestController
@RequestMapping("/v1/accessibility/multi")
public class MultiCriteriaController {
    private final Logger logger = LoggerFactory.getLogger(AggregateQueryController.class);

    /// <summary>
    /// Calculates simple multi-criteria accessibility based on gravity.
    /// </summary>
    @PostMapping
    public ResponseEntity<?> calcMultiCriteriaGrid(@RequestBody MultiCriteriaRequest request) {
        IRoutingProvider provider = RoutingManager.getRoutingProvider(request.routing);

        logger.debug("Creating PopulationView");
        IPopulationView view = PopulationManager.getPopulationView(request.population);
        if (view == null) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("accessibility/multi", "failed to get population-view, parameters are invalid"));
        }

        logger.debug("Creating GravityAccessibility");
        GravityAccessibility gravity = new GravityAccessibility();

        MultiCriteraAccessibility multiCriteria = new MultiCriteraAccessibility(view, gravity, provider);

        logger.debug("Adding Accessbilities");

        for (Map.Entry<String, InfrastructureParams> entry : request.infrastructures.entrySet()) {
            InfrastructureParams value = entry.getValue();
            multiCriteria.addAccessibility(entry.getKey(), value.facility_locations, value.ranges, value.range_factors,
                    value.infrastructure_weight);
        }
        logger.debug("Finished Adding Accessibilities");

        logger.debug("Building Response");
        multiCriteria.calcAccessibility();
        Map<String, Float>[] response = this.buildResponse(view, multiCriteria.getAccessibilities());
        logger.debug("Finished Building Response Grid");
        return ResponseEntity.ok(new MultiCriteriaResponse(response));
    }

    Map<String, Float>[] buildResponse(IPopulationView population, Map<String, Float>[] accessibilities) {
        Map<String, Float>[] features = new Map[population.pointCount()];

        for (int index = 0; index < population.pointCount(); index++) {
            Map<String, Float> values;
            if (accessibilities[index] != null) {
                values = accessibilities[index];
            } else {
                values = new HashMap<String, Float>();
                values.put("multiCritera", -9999.0f);
                values.put("multiCritera_weighted", -9999.0f);
            }
            features[index] = values;
        }

        return features;
    }
}