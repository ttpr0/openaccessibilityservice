package org.tud.oas.routing;

import java.util.List;

import org.tud.oas.demand.IDemandView;
import org.tud.oas.supply.ISupplyView;

public interface IRoutingProvider {
    void setProfile(String profile);

    void setRangeType(String range_type);

    void setOption(String name, Object value);

    /**
     * Computes the time-distance-matrix containing travel-times between all
     * supply and demand points.
     * 
     * @param demand
     * @param supply
     * @param ranges
     * @param mode
     * @return
     */
    ITDMatrix requestTDMatrix(IDemandView demand, ISupplyView supply, List<Double> ranges, String mode);

    /**
     * Computes the nearest supply to the demand points.
     * 
     * @param demand
     * @param supply
     * @param ranges
     * @param mode
     * @return
     */
    INNTable requestNearest(IDemandView demand, ISupplyView supply, List<Double> ranges, String mode);

    /**
     * Computes the k-nearest supplies to the demand points.
     * 
     * @param demand
     * @param supply
     * @param ranges
     * @param k
     * @param mode
     * @return
     */
    IKNNTable requestKNearest(IDemandView demand, ISupplyView supply, List<Double> ranges, int k,
            String mode);

    /**
     * Computes all supplies that lie within a given range to the demand
     * points.
     * 
     * @param demand
     * @param supply
     * @param range
     * @param mode
     * @return
     */
    ICatchment requestCatchment(IDemandView demand, ISupplyView supply, double range, String mode);
}