package org.tud.oas.demand.population;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;

public class PopulationAttributes {
    private Coordinate point;
    private Coordinate utm_point;
    private int[] population_values;

    public PopulationAttributes(Coordinate point, Coordinate utm_point, int[] population_values) {
        this.point = point;
        this.utm_point = utm_point;
        this.population_values = population_values;
    }

    public Coordinate getPoint() {
        return this.point;
    }

    public Coordinate getUTMPoint() {
        return this.utm_point;
    }

    public int getPopulationCount(int[] indizes) {
        int count = 0;
        for (int i = 0; i < indizes.length; i++) {
            count += this.population_values[indizes[i]];
        }
        return count;
    }

    public int getPopulationCount(int[] indizes, float[] factors) {
        int count = 0;
        for (int i = 0; i < indizes.length; i++) {
            count += factors[i] * this.population_values[indizes[i]];
        }
        return count;
    }
}
