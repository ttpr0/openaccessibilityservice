package org.tud.oas.routing;

import java.util.List;

import org.tud.oas.demand.IDemandView;
import org.tud.oas.supply.ISupplyView;

public interface IRoutingProvider {
    void setProfile(String profile);

    void setRangeType(String range_type);

    void setParameter(String name, Object value);

    /**
     * Computes the time-distance-matrix containing travel-times between all
     * supply and demand points.
     * Points that cannot be reached (either because of seperated sub-networks or
     * max search range will get a value < 0)
     * 
     * @param demand
     * @param supply
     * @param options
     * @return
     */
    ITDMatrix requestTDMatrix(IDemandView demand, ISupplyView supply, RoutingOptions options) throws Exception;

    /**
     * Computes the nearest supply to the demand points.
     * 
     * @param demand
     * @param supply
     * @param options
     * @return
     */
    INNTable requestNearest(IDemandView demand, ISupplyView supply, RoutingOptions options) throws Exception;

    /**
     * Computes the k-nearest supplies to the demand points.
     * 
     * @param demand
     * @param supply
     * @param k
     * @param options
     * @return
     */
    IKNNTable requestKNearest(IDemandView demand, ISupplyView supply, int k, RoutingOptions options) throws Exception;

    /**
     * Computes all supplies that lie within a given range to the demand
     * points.
     * 
     * @param demand
     * @param supply
     * @param range
     * @param options
     * @return
     */
    ICatchment requestCatchment(IDemandView demand, ISupplyView supply, double range, RoutingOptions options)
            throws Exception;
}