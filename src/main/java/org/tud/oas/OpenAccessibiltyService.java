package org.tud.oas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.tud.oas.demand.population.PopulationLoader;
import org.tud.oas.routing.ors.ORSProvider;
import org.tud.oas.services.DemandService;
import org.tud.oas.services.RoutingService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@SpringBootApplication
public class OpenAccessibiltyService {
	static final Logger logger = LoggerFactory.getLogger(OpenAccessibiltyService.class);

	public static void main(String[] args) throws Exception {
		RoutingService.setRoutingProvider(() -> {
			return new ORSProvider("http://172.26.62.41:8080/ors");
		});
		PopulationLoader.loadPopulation("./files/population_hannover.csv");
		DemandService.periodicClearViewStore(60 * 1000, 5 * 60 * 1000);

		SpringApplication.run(OpenAccessibiltyService.class, args);
	}

}
