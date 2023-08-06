package org.tud.oas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.tud.oas.population.PopulationManager;
import org.tud.oas.routing.RoutingManager;
import org.tud.oas.routing.ors.ORSProvider;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@SpringBootApplication
public class OpenAccessibiltyService {
	static final Logger logger = LoggerFactory.getLogger(OpenAccessibiltyService.class);

	public static void main(String[] args) throws Exception {
		RoutingManager.setRoutingProvider(() -> {
			return new ORSProvider("http://localhost:8082");
		});
		PopulationManager.loadPopulation("./files/population_hannover.csv");
		PopulationManager.periodicClearViewStore(60 * 1000, 5 * 60 * 1000);

		SpringApplication.run(OpenAccessibiltyService.class, args);
	}

}
