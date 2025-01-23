package org.tud.oas.api.multicriteria;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.tud.oas.accessibility.SimpleReachability;
import org.tud.oas.accessibility.distance_decay.IDistanceDecay;
import org.tud.oas.api.accessibility.AccessResponse;
import org.tud.oas.api.queries.aggregate.AggregateQueryController;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.requests.AccessResponseParams;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.RoutingOptions;
import org.tud.oas.services.DecayService;
import org.tud.oas.services.DemandService;
import org.tud.oas.services.RoutingService;
import org.tud.oas.services.SupplyService;
import org.tud.oas.supply.ISupplyView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/v1/multicriteria/multi")
public class MultiCriteriaController {
    private final Logger logger = LoggerFactory.getLogger(AggregateQueryController.class);

    private RoutingService routing_service;
    private DemandService demand_service;
    private SupplyService supply_service;
    private DecayService decay_service;

    @Autowired
    public MultiCriteriaController(RoutingService routing, DemandService demand, SupplyService supply,
            DecayService decay) {
        this.routing_service = routing;
        this.demand_service = demand;
        this.supply_service = supply;
        this.decay_service = decay;
    }

    @Operation(description = """
            Calculates simple multi-criteria accessibility based on gravity.
            """)
    @ApiResponse(responseCode = "200", description = "Standard response for successfully processed requests.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = MultiCriteriaResponse.class))
    })
    @PostMapping
    public MultiCriteriaResponse calcMultiCriteriaGrid(@RequestBody MultiCriteriaRequest request) {
        IRoutingProvider provider = routing_service.getRoutingProvider(request.routing);

        logger.debug("Creating DemandView");
        IDemandView demand_view = demand_service.getDemandView(request.demand);
        if (demand_view == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "failed to get population-view, parameters are invalid");
        }
        if (!AccessResponse.checkParams(request.response_params)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "response parameters are invalid");
        }

        logger.debug("Creating GravityAccessibility");
        SimpleReachability gravity = new SimpleReachability();

        logger.debug("Adding Accessbilities");
        Map<String, float[]> accessibilities = new HashMap<>();
        float[] multi_access = new float[demand_view.pointCount()];
        for (Map.Entry<String, InfrastructureParams> entry : request.infrastructures.entrySet()) {
            String name = entry.getKey();
            InfrastructureParams value = entry.getValue();
            ISupplyView supply_view = supply_service.getSupplyView(value.supply);
            if (supply_view == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "failed to get supply-view for " + name + ", parameters are invalid");
            }
            IDistanceDecay decay = decay_service.getDistanceDecay(value.decay);
            if (decay == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "failed to get distance-decay for " + name + ", parameters are invalid");
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

            float[] accessibility;
            try {
                accessibility = gravity.calcAccessibility(demand_view, supply_view, decay, provider, options);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
            if (request.return_all) {
                accessibilities.put(name, accessibility);
            }
            for (int i = 0; i < multi_access.length; i++) {
                if (accessibility[i] == -9999) {
                    continue;
                }
                multi_access[i] = multi_access[i] + value.infrastructure_weight * accessibility[i];
            }
        }
        accessibilities.put("multiCriteria", multi_access);
        logger.debug("Finished Adding Accessibilities");

        logger.debug("Building Response");
        Map<String, float[]> response = this.buildResponse(demand_view, accessibilities, request.response_params);
        logger.debug("Finished Building Response Grid");
        return new MultiCriteriaResponse(response);
    }

    Map<String, float[]> buildResponse(IDemandView demand, Map<String, float[]> accessibilities,
            AccessResponseParams params) {
        boolean scale = false;
        int[] scale_range = { 0, 100 };
        float no_data_value = -9999;
        if (params != null) {
            if (params.scale != null) {
                scale = params.scale;
            }
            if (scale && params.scale_range != null) {
                scale_range = params.scale_range;
            }
            if (params.no_data_value != null) {
                no_data_value = params.no_data_value;
            }
        }

        Map<String, float[]> new_map = new HashMap<>();
        for (Map.Entry<String, float[]> entry : accessibilities.entrySet()) {
            String name = entry.getKey();
            float[] access = entry.getValue();

            float max = -1000000000;
            float min = 1000000000;
            if (scale) {
                for (float w : access) {
                    if (w > max) {
                        max = w;
                    }
                    if (w < min) {
                        min = w;
                    }
                }
            }

            for (int i = 0; i < access.length; i++) {
                float val = access[i];
                if (val != 0) {
                    if (scale) {
                        val = (val + scale_range[0] - min)
                                * ((scale_range[1] - scale_range[0]) / (max - min));
                    }
                } else {
                    val = no_data_value;
                }
                access[i] = val;
            }
            new_map.put(name, access);
        }
        return new_map;
    }
}