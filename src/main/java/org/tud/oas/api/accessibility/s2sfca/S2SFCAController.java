package org.tud.oas.api.accessibility.s2sfca;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.tud.oas.accessibility.fca.Simple2SFCA;
import org.tud.oas.api.accessibility.AccessResponse;
import org.tud.oas.demand.IDemandView;
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
@RequestMapping("/v1/accessibility/2sfca")
public class S2SFCAController {
	private final Logger logger = LoggerFactory.getLogger(S2SFCAController.class);

	private RoutingService routing_service;
	private DemandService demand_service;
	private SupplyService supply_service;
	private DecayService decay_service;

	@Autowired
	public S2SFCAController(RoutingService routing, DemandService demand, SupplyService supply,
			DecayService decay) {
		this.routing_service = routing;
		this.demand_service = demand;
		this.supply_service = supply;
		this.decay_service = decay;
	}

	@Operation(description = """
			Calculates simple two-step-floating-catchment-area.
			""")
	@ApiResponse(responseCode = "200", description = "Standard response for successfully processed requests.", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = AccessResponse.class))
	})
	@PostMapping
	public AccessResponse calcFCA(@RequestBody S2SFCARequest request) {
		logger.info("Run FCA Request");

		IDemandView demand_view = demand_service.getDemandView(request.demand);
		if (demand_view == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"failed to get demand-view, parameters are invalid");
		}
		ISupplyView supply_view = supply_service.getSupplyView(request.supply);
		if (supply_view == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"failed to get supply-view, parameters are invalid");
		}
		IRoutingProvider provider = routing_service.getRoutingProvider(request.routing);
		Float catchment = request.catchment;
		if (catchment == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"failed to get catchment size, parameter is missing");
		}
		RoutingOptions options = new RoutingOptions("isochrones", (double) catchment);

		float[] weights = Simple2SFCA.calc2SFCA(demand_view, supply_view, catchment, provider, options);

		return new AccessResponse(weights, demand_view, request.response_params);
	}
}
