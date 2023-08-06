package org.tud.oas.accessibility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tud.oas.population.IPopulationView;
import org.tud.oas.routing.IRoutingProvider;

public class MultiCriteraAccessibility {

    private IPopulationView population;
    private GravityAccessibility gravity;
    private IRoutingProvider provider;
    private float max_population;
    private float max_value;
    private float max_weighted_value;
    private Map<String, Float>[] accessibilities;

    public MultiCriteraAccessibility(IPopulationView population, GravityAccessibility gravity,
            IRoutingProvider provider) {
        this.population = population;
        this.gravity = gravity;
        this.provider = provider;

        float max_pop = 100;
        this.max_population = max_pop;

        this.accessibilities = new Map[population.pointCount()];
    }

    public Map<String, Float>[] getAccessibilities() {
        return this.accessibilities;
    }

    public void addAccessibility(String name, double[][] facilities, List<Double> ranges, List<Double> factors,
            double weight) {
        Access[] accessibility;
        try {
            accessibility = this.gravity.calcAccessibility(this.population, facilities, ranges, factors,
                    this.provider);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Access defaultAccess = new Access();
        defaultAccess.access = -9999;
        defaultAccess.weighted_access = -9999;
        for (int index = 0; index < accessibility.length; index++) {
            Access access = accessibility[index] != null ? accessibility[index] : defaultAccess;
            Map<String, Float> multi_access;
            if (this.accessibilities[index] == null) {
                this.accessibilities[index] = new HashMap<String, Float>();
                multi_access = this.accessibilities[index];
                multi_access.put("multiCritera", 0.0f);
                multi_access.put("multiCritera_weighted", 0.0f);
            } else {
                multi_access = this.accessibilities[index];
            }
            multi_access.put(name, access.access);
            multi_access.put(name + "_weighted", access.weighted_access);
            if (access.access == -9999) {
                continue;
            }
            float temp = multi_access.get("multiCritera");
            float weighted_temp = multi_access.get("multiCritera_weighted");
            float new_value = temp + access.access;
            float new_weighted_value = weighted_temp + access.access * population.getPopulation(index) / max_population;
            multi_access.put("multiCritera", new_value);
            multi_access.put("multiCritera_weighted", new_weighted_value);
            if (new_value > max_value) {
                max_value = new_value;
            }
            if (new_weighted_value > max_weighted_value) {
                max_weighted_value = new_weighted_value;
            }
        }
    }

    public void calcAccessibility() {
        for (int index = 0; index < this.accessibilities.length; index++) {
            Map<String, Float> multi_access = this.accessibilities[index];
            float temp = multi_access.get("multiCritera");
            float weighted_temp = multi_access.get("multiCritera_weighted");
            if (temp == 0.0) {
                multi_access.put("multiCritera", -9999.0f);
                multi_access.put("multiCritera_weighted", -9999.0f);
            } else {
                multi_access.put("multiCritera", temp * 100 / max_value);
                multi_access.put("multiCritera_weighted", weighted_temp * 100 / max_weighted_value);
            }
        }
    }
}
