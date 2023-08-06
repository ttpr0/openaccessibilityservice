package org.tud.oas.api.accessibility.simple;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tud.oas.api.responses.ErrorResponse;
import org.tud.oas.population.IPopulationView;
import org.tud.oas.population.PopulationManager;
import org.tud.oas.routing.IKNNTable;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.RoutingManager;

@RestController()
@RequestMapping("/v1/accessibility/simple")
public class SimpleAccessibilityController {
    /// <summary>
    /// Calculates simple accessibility.
    /// </summary>
    @PostMapping
    public ResponseEntity<?> calcSimpleGrid(@RequestBody SimpleAccessibilityRequest request) {
        IRoutingProvider provider = RoutingManager.getRoutingProvider(request.routing);
        IPopulationView view = PopulationManager.getPopulationView(request.population);
        if (view == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("accessibility/gravity/grid",
                    "failed to get population-view, parameters are invalid"));
        }

        IKNNTable table = provider.requestKNearest(view, request.facility_locations, request.ranges, 3, "isochrones");

        SimpleValue[] response = this.buildResponse(view, table);

        return ResponseEntity.ok(new SimpleAccessibilityResponse(response));
    }

    SimpleValue[] buildResponse(IPopulationView population, IKNNTable table) {
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
