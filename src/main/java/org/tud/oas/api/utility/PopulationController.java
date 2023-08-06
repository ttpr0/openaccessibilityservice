package org.tud.oas.api.utility;

import org.locationtech.jts.geom.Coordinate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.tud.oas.population.IPopulationView;
import org.tud.oas.population.PopulationManager;
import org.tud.oas.api.responses.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/utility/population")
public class PopulationController {

    /// <summary>
    /// Stores a population view for use in other requests.
    /// </summary>
    @PostMapping("/store")
    public ResponseEntity<?> storePopulationView(@RequestBody PopulationStoreRequest request) {
        IPopulationView view = PopulationManager.getPopulationView(request.population);
        if (view == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("utility/population/store",
                    "failed to get population-view, parameters are invalid"));
        }

        UUID id = PopulationManager.storePopulationView(view);
        return ResponseEntity.ok(new PopulationStoreResponse(id));
    }

    /// <summary>
    /// Retrives data from stored or internal population view.
    /// </summary>
    @PostMapping("/get")
    public ResponseEntity<?> getPopulationView(@RequestBody PopulationGetRequest request) {
        IPopulationView view;

        if (request.population_id != null) {
            view = PopulationManager.getStoredPopulationView(request.population_id);
            if (view == null) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("utility/population/get", "no stored population-view found"));
            }
        } else {
            view = PopulationManager.getPopulationView(request.population);
            if (view == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("utility/population/get",
                        "failed to get population-view, parameters are invalid"));
            }
        }

        List<double[]> locations = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        for (int i = 0; i < view.pointCount(); i++) {
            int index = i;
            Coordinate point = view.getCoordinate(index);
            double weight = view.getPopulation(index);

            locations.add(new double[] { point.getX(), point.getY() });
            weights.add(weight);
        }

        return ResponseEntity.ok(new PopulationGetResponse(locations, weights));
    }
}
