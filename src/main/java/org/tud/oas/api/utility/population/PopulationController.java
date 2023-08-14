package org.tud.oas.api.utility.population;

import org.locationtech.jts.geom.Coordinate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tud.oas.api.responses.*;
import org.tud.oas.demand.IDemandView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.tud.oas.demand.DemandManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/utility/population")
public class PopulationController {

    @Operation(description = """
            Stores a population view for use in other requests.
            """)
    @ApiResponse(responseCode = "200", description = "Standard response for successfully processed requests.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = PopulationStoreResponse.class))
    })
    @ApiResponse(responseCode = "400", description = "The request is incorrect and therefore can not be processed.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    })
    @PostMapping("/store")
    public ResponseEntity<?> storePopulationView(@RequestBody PopulationStoreRequest request) {
        IDemandView view = DemandManager.getDemandView(request.population);
        if (view == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("utility/population/store",
                    "failed to get population-view, parameters are invalid"));
        }

        UUID id = DemandManager.storeDemandView(view);
        return ResponseEntity.ok(new PopulationStoreResponse(id));
    }

    @Operation(description = """
            Retrives data from stored or internal population view.
            """)
    @ApiResponse(responseCode = "200", description = "Standard response for successfully processed requests.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = PopulationGetResponse.class))
    })
    @ApiResponse(responseCode = "400", description = "The request is incorrect and therefore can not be processed.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    })
    @PostMapping("/get")
    public ResponseEntity<?> getPopulationView(@RequestBody PopulationGetRequest request) {
        IDemandView view;

        if (request.population_id != null) {
            view = DemandManager.getStoredDemandView(request.population_id);
            if (view == null) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("utility/population/get", "no stored population-view found"));
            }
        } else {
            view = DemandManager.getDemandView(request.population);
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
            double weight = view.getDemand(index);

            locations.add(new double[] { point.getX(), point.getY() });
            weights.add(weight);
        }

        return ResponseEntity.ok(new PopulationGetResponse(locations, weights));
    }
}
