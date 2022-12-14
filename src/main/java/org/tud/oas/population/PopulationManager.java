package org.tud.oas.population;

public class PopulationManager {
    private static Population population;

    public static void loadPopulation(String filename) throws Exception {
        PopulationManager.population = PopulationLoader.loadFromCSV(filename);
    }

    public static Population getPopulation() {
        return PopulationManager.population;
    }
}
