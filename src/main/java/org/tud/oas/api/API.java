package org.tud.oas.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tud.oas.fca.Simple2SFCA;
import org.tud.oas.population.Population;
import org.tud.oas.population.PopulationManager;

import ch.qos.logback.core.encoder.EchoEncoder;

@RestController
@RequestMapping("/v1/test")
public class API {

    @GetMapping("/get")
    public String getTest() {
        return "hello world";
    }

    @PostMapping("/fca")
    public FCAGeoJSONResponse calculateFCA(@RequestBody FCARequest request) throws Exception {
        Population population = PopulationManager.getPopulation();

		float[] weights = Simple2SFCA.calc2SFCA(population, request.getLocations(), request.getRanges(), request.getFactors());
    
        float max_weight = 0;
        for (float w : weights) {
            if (w>max_weight) {
                max_weight = w;
            }
        }
        float factor = 100/max_weight;
        for (int i=0; i<weights.length; i++) {
            if (weights[i] != -1) {
                weights[i] = weights[i]*factor;
            }
        }
        return new FCAGeoJSONResponse(population, weights);
    }
}
