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
import org.tud.oas.accessibility.SimpleReachability;
import org.tud.oas.accessibility.MultiCriteraReachability;
import org.tud.oas.api.queries.aggregate.AggregateQueryController;
import org.tud.oas.api.responses.ErrorResponse;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.demand.DemandManager;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.RoutingManager;
import org.tud.oas.supply.ISupplyView;
import org.tud.oas.supply.SupplyManager;

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

        logger.debug("Creating DemandView");
        IDemandView demand_view = DemandManager.getDemandView(request.demand);
        if (demand_view == null) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("accessibility/multi", "failed to get population-view, parameters are invalid"));
        }

        logger.debug("Creating GravityAccessibility");
        SimpleReachability gravity = new SimpleReachability();

        MultiCriteraReachability multiCriteria = new MultiCriteraReachability(demand_view, gravity, provider);

        logger.debug("Adding Accessbilities");

        for (Map.Entry<String, InfrastructureParams> entry : request.infrastructures.entrySet()) {
            InfrastructureParams value = entry.getValue();
            ISupplyView supply_view = SupplyManager.getSupplyView(value.supply);
            multiCriteria.addAccessibility(entry.getKey(), (float) value.infrastructure_weight, supply_view,
                    value.ranges, value.decay);
        }
        logger.debug("Finished Adding Accessibilities");

        logger.debug("Building Response");
        multiCriteria.calcAccessibility();
        Map<String, float[]> response = this.buildResponse(demand_view, multiCriteria.getAccessibilities());
        logger.debug("Finished Building Response Grid");
        return ResponseEntity.ok(new MultiCriteriaResponse(response));
    }

    Map<String, float[]> buildResponse(IDemandView demand, Map<String, float[]> accessibilities) {
        float[] multi_access;
        if (accessibilities.containsKey("multiCriteria")) {
            multi_access = accessibilities.get("multiCriteria");
        } else {
            multi_access = new float[demand.pointCount()];
        }
        float max_value = 0;
        for (int i = 0; i < multi_access.length; i++) {
            if (multi_access[i] == -9999) {
                continue;
            }
            if (multi_access[i] > max_value) {
                max_value = multi_access[i];
            }
        }
        for (int i = 0; i < multi_access.length; i++) {
            if (multi_access[i] == -9999) {
                continue;
            }
            multi_access[i] = multi_access[i] * 100 / max_value;
        }
        accessibilities.put("multiCriteria", multi_access);
        return accessibilities;
    }
}