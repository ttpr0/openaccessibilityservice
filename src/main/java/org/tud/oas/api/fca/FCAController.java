package org.tud.oas.api.fca;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tud.oas.accessibility.Enhanced2SFCA;
import org.tud.oas.accessibility.distance_decay.DistanceDecay;
import org.tud.oas.accessibility.distance_decay.IDistanceDecay;
import org.tud.oas.api.queries.aggregate.AggregateQueryController;
import org.tud.oas.api.responses.ErrorResponse;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.demand.DemandManager;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.RoutingManager;
import org.tud.oas.routing.RoutingOptions;
import org.tud.oas.supply.ISupplyView;
import org.tud.oas.supply.SupplyManager;

@RestController
@RequestMapping("/v1/fca")
public class FCAController {
    private final Logger logger = LoggerFactory.getLogger(FCAController.class);

    @PostMapping
    public ResponseEntity<?> calcFCA(@RequestBody FCARequest request) {
        logger.info("Run FCA Request");

        IDemandView demand_view = DemandManager.getDemandView(request.demand);
        if (demand_view == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("2sfca/enhanced", "failed to get demand-view, parameters are invalid"));
        }
        ISupplyView supply_view = SupplyManager.getSupplyView(request.supply);
        if (supply_view == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("2sfca/enhanced",
                    "failed to get supply-view, parameters are invalid"));
        }
        IRoutingProvider provider = RoutingManager.getRoutingProvider(request.routing);
        IDistanceDecay decay = DistanceDecay.getDistanceDecay(request.distance_decay);
        if (decay == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("2sfca/enhanced", "failed to get distance-decay, parameters are invalid"));
        }
        RoutingOptions options;
        if (decay.getDistances() == null) {
            options = new RoutingOptions(request.mode, (double) decay.getMaxDistance());
        } else {
            float[] distances = decay.getDistances();
            List<Double> ranges = new ArrayList(distances.length);
            for (int i = 0; i < distances.length; i++) {
                ranges.add((double) distances[i]);
            }
            options = new RoutingOptions(request.mode, ranges);
        }

        float[] weights = Enhanced2SFCA.calc2SFCA(demand_view, supply_view, decay, provider, options);

        float[] response = buildResponse(demand_view, weights);
        return ResponseEntity.ok(new FCAResponse(response));
    }

    private float[] buildResponse(IDemandView population, float[] accessibilities) {
        float maxWeight = 0;
        for (int i = 0; i < accessibilities.length; i++) {
            float w = accessibilities[i];
            if (w > maxWeight) {
                maxWeight = w;
            }
        }
        // for (float w : accessibilities) {
        // if (w > maxWeight) {
        // maxWeight = w;
        // }
        // }
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