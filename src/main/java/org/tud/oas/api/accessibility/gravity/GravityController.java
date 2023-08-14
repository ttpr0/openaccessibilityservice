package org.tud.oas.api.accessibility.gravity;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.tud.oas.accessibility.SimpleReachability;
import org.tud.oas.accessibility.distance_decay.DistanceDecay;
import org.tud.oas.accessibility.distance_decay.IDistanceDecay;
import org.tud.oas.api.queries.aggregate.AggregateQueryController;
import org.tud.oas.api.responses.ErrorResponse;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.demand.DemandManager;
import org.tud.oas.routing.RoutingManager;
import org.tud.oas.routing.RoutingOptions;
import org.tud.oas.supply.ISupplyView;
import org.tud.oas.supply.SupplyManager;
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
        IDemandView demand_view = DemandManager.getDemandView(request.demand);
        if (demand_view == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("accessibility/gravity",
                    "failed to get demand-view, parameters are invalid"));
        }
        ISupplyView supply_view = SupplyManager.getSupplyView(request.supply);
        if (supply_view == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("accessibility/gravity",
                    "failed to get supply-view, parameters are invalid"));
        }
        IRoutingProvider provider = RoutingManager.getRoutingProvider(request.routing);
        IDistanceDecay decay = DistanceDecay.getDistanceDecay(request.distance_decay);
        if (decay == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("accessibility/gravity",
                            "failed to get distance-decay, parameters are invalid"));
        }
        RoutingOptions options;
        if (decay.getDistances() == null) {
            options = new RoutingOptions("matrix", (double) decay.getMaxDistance());
        } else {
            float[] distances = decay.getDistances();
            List<Double> ranges = new ArrayList(distances.length);
            for (int i = 0; i < distances.length; i++) {
                ranges.add((double) distances[i]);
            }
            options = new RoutingOptions("isochrones", ranges);
        }

        logger.debug("start calculation gravity accessibility");
        SimpleReachability gravity = new SimpleReachability();
        float[] access = gravity.calcAccessibility(demand_view, supply_view, decay, provider, options);

        logger.debug("start building response");
        float[] response = this.buildResponse(demand_view, access);
        logger.debug("response build successfully");

        return ResponseEntity.ok(new GravityResponse(response));
    }

    private float[] buildResponse(IDemandView population, float[] accessibilities) {
        float maxWeight = 0;
        for (float w : accessibilities) {
            if (w > maxWeight) {
                maxWeight = w;
            }
        }
        float factor = 100 / maxWeight;

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