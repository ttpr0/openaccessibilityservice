package org.tud.oas.api.accessibility;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.tud.oas.accessibility.Accessibility;
import org.tud.oas.accessibility.GravityAccessibility;
import org.tud.oas.accessibility.PopulationAccessibility;
import org.tud.oas.accessibility.SimpleAccessibility;
import org.tud.oas.population.Population;
import org.tud.oas.population.PopulationManager;
import org.tud.oas.population.PopulationPoint;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.RoutingManager;

@RestController
@RequestMapping("/v1/accessibility")
public class AccessibilityController {

    @PostMapping("")
    public GridResponse calcGrid(@RequestBody SimpleAccessibilityRequest request) throws Exception {
        return this.calcSimpleGrid(request);
    }

    @PostMapping("/simple")
    public GridResponse calcSimpleGrid(@RequestBody SimpleAccessibilityRequest request) throws Exception {
        Population population = PopulationManager.getPopulation();
        IRoutingProvider provider = RoutingManager.getRoutingProvider();

        SimpleAccessibility simple = new SimpleAccessibility(population, provider);

		simple.calcAccessibility(request.getLocations(), request.getRanges());
        
        return simple.buildResponse();
    }

    @PostMapping("/gravity")
    public GridResponse calcGravityGrid(@RequestBody GravityAccessibilityRequest request) throws Exception {
        Population population = PopulationManager.getPopulation();
        IRoutingProvider provider = RoutingManager.getRoutingProvider();

        GravityAccessibility gravity = new GravityAccessibility(population, provider);

		gravity.calcAccessibility(request.getLocations(), request.getRanges(), request.getFactors());
        
        return gravity.buildResponse();
    }
}
