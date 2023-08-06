package org.tud.oas.routing;

import org.tud.oas.population.IPopulationView;

import java.util.List;

public interface IRoutingProvider {
    void setProfile(String profile);

    void setRangeType(String range_type);

    void setOption(String name, Object value);

    /**
     * Computes the time-distance-matrix containing travel-times between all
     * facilities and population points.
     * 
     * @param population
     * @param facilities
     * @param ranges
     * @param mode
     * @return
     */
    ITDMatrix requestTDMatrix(IPopulationView population, double[][] facilities, List<Double> ranges, String mode);

    /**
     * Computes the nearest facilities to the population points.
     * 
     * @param population
     * @param facilities
     * @param ranges
     * @param mode
     * @return
     */
    INNTable requestNearest(IPopulationView population, double[][] facilities, List<Double> ranges, String mode);

    /**
     * Computes the k-nearest facilities to the population points.
     * 
     * @param population
     * @param facilities
     * @param ranges
     * @param k
     * @param mode
     * @return
     */
    IKNNTable requestKNearest(IPopulationView population, double[][] facilities, List<Double> ranges, int k,
            String mode);

    /**
     * Computes all facilities that lie within a given range to the population
     * points.
     * 
     * @param population
     * @param facilities
     * @param range
     * @param mode
     * @return
     */
    ICatchment requestCatchment(IPopulationView population, double[][] facilities, double range, String mode);
}