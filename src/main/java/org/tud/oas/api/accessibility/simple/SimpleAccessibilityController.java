package org.tud.oas.api.accessibility.simple;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tud.oas.api.responses.ErrorResponse;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.demand.DemandManager;
import org.tud.oas.routing.IKNNTable;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.RoutingManager;
import org.tud.oas.routing.RoutingOptions;
import org.tud.oas.supply.ISupplyView;
import org.tud.oas.supply.SupplyManager;

@RestController()
@RequestMapping("/v1/accessibility/simple")
public class SimpleAccessibilityController {
    /// <summary>
    /// Calculates simple accessibility.
    /// </summary>
    @PostMapping
    public ResponseEntity<?> calcSimpleGrid(@RequestBody SimpleAccessibilityRequest request) {
        IRoutingProvider provider = RoutingManager.getRoutingProvider(request.routing);
        IDemandView demand_view = DemandManager.getDemandView(request.demand);
        if (demand_view == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("accessibility/gravity/grid",
                    "failed to get demand-view, parameters are invalid"));
        }
        ISupplyView supply_view = SupplyManager.getSupplyView(request.supply);
        if (supply_view == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("accessibility/gravity",
                    "failed to get supply-view, parameters are invalid"));
        }

        IKNNTable table = provider.requestKNearest(demand_view, supply_view, 3,
                new RoutingOptions("isochrones", request.ranges));

        SimpleValue[] response = this.buildResponse(demand_view, table);

        return ResponseEntity.ok(new SimpleAccessibilityResponse(response));
    }

    SimpleValue[] buildResponse(IDemandView population, IKNNTable table) {
        SimpleValue[] features = new SimpleValue[population.pointCount()];
        for (int i = 0; i < population.pointCount(); i++) {
            int index = i;
            float[] ranges = new float[3];
            for (int j = 0; j < 3; j++) {
                ranges[j] = table.getKNearestRange(index, j);
            }
            SimpleValue value = new SimpleValue(-9999, -9999, -9999);
            value.first = (int) ranges[0];
            value.second = (int) ranges[1];
            value.third = (int) ranges[2];
            features[index] = value;
        }

        return features;
    }
}