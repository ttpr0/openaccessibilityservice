package org.tud.oas.population;

public class PopulationAttributes {
    private int index;
    private int population_count;

    public PopulationAttributes(int population_count) {
        this.population_count = population_count;
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
}
