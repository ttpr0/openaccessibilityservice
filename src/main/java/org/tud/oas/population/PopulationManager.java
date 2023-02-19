package org.tud.oas.population;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PopulationManager {
    static final Logger logger = LoggerFactory.getLogger(PopulationManager.class);
    private static Population population;

    public static void loadPopulation(String filename) throws Exception {
        logger.info("load Population data");
        PopulationManager.population = PopulationLoader.loadFromCSV(filename);
        logger.info("finished loading Population data");
    }

    public static Population getPopulation() {
        return PopulationManager.population;
    }
}
