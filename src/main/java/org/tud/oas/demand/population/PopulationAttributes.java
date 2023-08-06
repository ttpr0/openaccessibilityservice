package org.tud.oas.demand.population;

public class PopulationAttributes {
    private int index;
    private int population_count;
    private int[] standard_population;
    private int[] kita_schul_population;

    public PopulationAttributes(int population_count, int[] standard_population, int[] kita_schul_population) {
        this.population_count = population_count;
        this.standard_population = standard_population;
        this.kita_schul_population = kita_schul_population;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
    public int getPopulationCount() {
        return population_count;
    }
    public void setPopulationCount(int population_count) {
        this.population_count = population_count;
    }

    public int getStandardPopulation(int[] indizes) {
        int count = 0;
        for (int i=0; i<indizes.length; i++) {
            count += this.standard_population[indizes[i]];
        }
        return count;
    }

    public int getKitaSchulPopulation(int[] indizes) {
        int count = 0;
        for (int i=0; i<indizes.length; i++) {
            count += this.kita_schul_population[indizes[i]];
        }
        return count;
    }
}
