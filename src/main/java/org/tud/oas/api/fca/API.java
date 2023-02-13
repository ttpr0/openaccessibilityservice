package org.tud.oas.api.fca;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tud.oas.fca.Simple2SFCA;
import org.tud.oas.population.Population;
import org.tud.oas.population.PopulationManager;

@RestController
@RequestMapping("/v1/fca")
public class API {

    @PostMapping
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
