package org.tud.oas.accessibility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tud.oas.accessibility.distance_decay.DecayRequestParams;
import org.tud.oas.accessibility.distance_decay.DistanceDecay;
import org.tud.oas.accessibility.distance_decay.IDistanceDecay;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.supply.ISupplyView;

/**
 * Computes a multi-criteria result from multiple weighted reachability queries.
 */
public class MultiCriteraReachability {

    private IDemandView demand;
    private SimpleReachability gravity;
    private IRoutingProvider provider;
    private Map<String, float[]> accessibilities;

    public MultiCriteraReachability(IDemandView demand, SimpleReachability gravity,
            IRoutingProvider provider) {
        this.demand = demand;
        this.gravity = gravity;
        this.provider = provider;

        this.accessibilities = new HashMap();
    }

    /**
     * Returns a Map containing both the individuel reachabilities and the
     * mutli-criteria result (key 'multiCriteria').
     * 
     * @return accessibility map.
     */
    public Map<String, float[]> getAccessibilities() {
        return this.accessibilities;
    }

    /**
     * Adds a reachability to the mutli-criteria value.
     * 
     * @param name         Name of the reachability (stored in the accessibility map
     *                     under this name).
     * @param weight       Multi-criteria weight of the results.
     * @param supply       Supply locations and weights (used in reachability
     *                     query).
     * @param ranges       Ranges of isochrones used in computation (used in
     *                     reachability query).
     * @param decay_params Distande decay (used in reachability query).
     */
    public void addAccessibility(String name, float weight, ISupplyView supply, List<Double> ranges,
            DecayRequestParams decay_params) {
        float[] accessibility;
        IDistanceDecay decay = DistanceDecay.getDistanceDecay(decay_params);
        try {
            accessibility = this.gravity.calcAccessibility(this.demand, supply, ranges, decay, this.provider);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        this.accessibilities.put(name, accessibility);
        float[] multi_access;
        if (this.accessibilities.containsKey("multiCriteria")) {
            multi_access = this.accessibilities.get("multiCriteria");
        } else {
            multi_access = new float[this.demand.pointCount()];
        }
        for (int i = 0; i < multi_access.length; i++) {
            if (accessibility[i] == -9999) {
                continue;
            }
            multi_access[i] = multi_access[i] + weight * accessibility[i];
        }
        this.accessibilities.put("multiCriteria", multi_access);
    }

    /**
     * Should be called after the last call to addAccessibility.
     * Changes values of all 'unvisited' demand points to -9999.
     */
    public void calcAccessibility() {
        float[] multi_access;
        if (this.accessibilities.containsKey("multiCriteria")) {
            multi_access = this.accessibilities.get("multiCriteria");
        } else {
            multi_access = new float[this.demand.pointCount()];
        }
        for (int i = 0; i < multi_access.length; i++) {
            if (multi_access[i] <= 0) {
                multi_access[i] = -9999;
            }
        }
        this.accessibilities.put("multiCriteria", multi_access);
    }
}
