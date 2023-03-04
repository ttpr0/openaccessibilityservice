package org.tud.oas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.tud.oas.fca.Simple2SFCA;
import org.tud.oas.population.Population;
import org.tud.oas.population.PopulationLoader;
import org.tud.oas.population.PopulationManager;
import org.tud.oas.routing.ORSProvider;
import org.tud.oas.routing.RoutingManager;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@SpringBootApplication
public class OpenAccessibiltyService {
	static final Logger logger = LoggerFactory.getLogger(OpenAccessibiltyService.class);

	public static void main(String[] args) throws Exception {
		RoutingManager.addRoutingProvider(new ORSProvider("http://172.26.62.41:8080/ors"));
		// RoutingManager.addRoutingProvider(new ORSProvider());
		PopulationManager.loadPopulation("files/population_hannover.csv");
		SpringApplication.run(OpenAccessibiltyService.class, args);
	}

}
