package org.tud.oas.api.queries.aggregate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.routing.ICatchment;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.RoutingOptions;
import org.tud.oas.services.DemandService;
import org.tud.oas.services.RoutingService;
import org.tud.oas.services.SupplyService;
import org.tud.oas.supply.ISupplyView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/v1/queries/aggregate")
public class AggregateQueryController {
	private final Logger logger = LoggerFactory.getLogger(AggregateQueryController.class);

	private RoutingService routing_service;
	private DemandService demand_service;
	private SupplyService supply_service;

	@Autowired
	public AggregateQueryController(RoutingService routing, DemandService demand, SupplyService supply) {
		this.routing_service = routing;
		this.demand_service = demand;
		this.supply_service = supply;
	}

	@Operation(description = """
			Calculates aggregate query.
			""")
	@ApiResponse(responseCode = "200", description = "Standard response for successfully processed requests.", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = AggregateQueryResponse.class))
	})
	@PostMapping
	public AggregateQueryResponse calcQuery(@RequestBody AggregateQueryRequest request) {
		ICatchment catchment;
		IDemandView demand_view;
		ISupplyView supply_view = supply_service.getSupplyView(request.supply);
		if (supply_view == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"failed to get supply-view, parameters are invalid");
		}

		demand_view = demand_service.getDemandView(request.demand);
		if (demand_view == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"failed to get population-view, parameters are invalid");
		}

		IRoutingProvider provider = routing_service.getRoutingProvider(request.routing);
		try {
			catchment = provider.requestCatchment(demand_view, supply_view, request.range,
					new RoutingOptions("isochrones"));
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}

		float[] results = AggregateQuery.computeQuery(supply_view, catchment, request.compute_type);

		return new AggregateQueryResponse(results);
	}
}
