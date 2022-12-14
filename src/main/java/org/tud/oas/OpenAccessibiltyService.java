package org.tud.oas;

import org.heigit.ors.routing.RoutingProfileManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.vividsolutions.jts.geom.Coordinate;

import org.tud.oas.fca.Simple2SFCA;
import org.tud.oas.population.Population;
import org.tud.oas.population.PopulationLoader;
import org.tud.oas.population.PopulationManager;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

@SpringBootApplication
public class OpenAccessibiltyService {

	public static void main(String[] args) throws Exception {
		RoutingProfileManager.getInstance();
		PopulationManager.loadPopulation("files/population_hannover.csv");
		SpringApplication.run(OpenAccessibiltyService.class, args);

		// Population population = PopulationManager.getPopulation();

		// Double[][] facilities = new Double[][] {
		// 	{9.7972148950000246,52.390422817000058},
		// 	{9.8103311970000391,52.37191468900005},
		// 	{9.7401107480000633,52.444164456000067},
		// };

		// List<Double> ranges = Arrays.asList(100.0, 200.0, 300.0, 400.0, 500.0, 600.0, 700.0, 800.0, 900.0, 1000.0);
		// List<Double> factors = Arrays.asList(1.0, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1);

		// float[] weights = Simple2SFCA.calc2SFCA(population, facilities, ranges, factors);

		// System.out.println("hello");
	}

}
