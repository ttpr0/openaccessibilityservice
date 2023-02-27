package org.tud.oas.accessibility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.tud.oas.api.accessibility.GridFeature;
import org.tud.oas.api.accessibility.GridResponse;
import org.tud.oas.population.Population;
import org.tud.oas.population.PopulationAttributes;
import org.tud.oas.population.PopulationPoint;

public class MultiCriteraAccessibility {
    private Population population;
    private GravityAccessibility gravity;

    private float[] population_weights;
    private float max_population;
    private float max_value;
    private float max_weighted_value;

    private HashMap<String, Float>[] accessibilities;

    public MultiCriteraAccessibility(Population population, GravityAccessibility gravity) {
        this.population = population;
        this.gravity = gravity;

        float[] population_weights = new float[population.getPointCount()];
        float max_pop = 0;
        for (PopulationAttributes attr : population.attributes) {
            int pop_weight = attr.getPopulationCount();
            if (pop_weight == 0) {
                pop_weight = 1;
            }
            int index = attr.getIndex();
            population_weights[index] = pop_weight;
            if (pop_weight > max_pop) {
                max_pop = pop_weight;
            }
        }

        this.population_weights = population_weights;
        this.max_population = max_pop;

        this.accessibilities = new HashMap[population.getPointCount()];
        for (int i = 0; i < accessibilities.length; i++) {
            this.accessibilities[i] = new HashMap<String, Float>();
            this.accessibilities[i].put("multiCritera", 0.0f);
            this.accessibilities[i].put("multiCritera_weighted", 0.0f);
        }
    }

    public void addAccessibility(String name, Double[][] facilities, List<Double> ranges, List<Double> factors, double weight) throws Exception {
        gravity.calcAccessibility(facilities, ranges, factors);
        float[] access = gravity.getAccessibility();
        float[] weighted_access = gravity.getWeightedAccessibility();

        for (int i=0; i<accessibilities.length; i++) {
            accessibilities[i].put(name, access[i]);
            accessibilities[i].put(name + "_weighted", weighted_access[i]);
            if (access[i] == -9999) {
                continue;
            }
            float temp = accessibilities[i].get("multiCritera");
            float weighted_temp = accessibilities[i].get("multiCritera_weighted");
            float new_value = temp + access[i];
            float new_weighted_value = weighted_temp + access[i] * population_weights[i] / max_population;
            accessibilities[i].put("multiCritera", new_value);
            accessibilities[i].put("multiCritera_weighted", new_weighted_value);
            if (new_value > max_value) {
                max_value = new_value;
            }
            if (new_weighted_value > max_weighted_value) {
                max_weighted_value = new_weighted_value;
            }
        }
    }

    public void calcAccessibility() {
        for (int i=0; i<accessibilities.length; i++) {
            float temp = accessibilities[i].get("multiCritera");
            float weighted_temp = accessibilities[i].get("multiCritera_weighted");
            if (temp == 0.0) {
                accessibilities[i].put("multiCritera", -9999.0f);
                accessibilities[i].put("multiCritera_weighted", -9999.0f);
            }
            else {
                accessibilities[i].put("multiCritera", temp * 100 / max_value);
                accessibilities[i].put("multiCritera_weighted", weighted_temp * 100 / max_weighted_value);
            }
        }
    }

    public GridResponse buildResponse() {
        List<GridFeature> features = new ArrayList<GridFeature>();
        float minx = 1000000000;
        float maxx = -1;
        float miny = 1000000000;
        float maxy = -1;
        for (int i=0; i< population.getPointCount(); i++) {
            PopulationPoint p = population.getPoint(i);
            HashMap<String, Float> values = this.accessibilities[i];
            if (p.getX() < minx) {
                minx = p.getX();
            }
            if (p.getX() > maxx) {
                maxx = p.getX();
            }
            if (p.getY() < miny) {
                miny = p.getY();
            }
            if (p.getY() > maxy) {
                maxy = p.getY();
            }
            features.add(new GridFeature(p.getX(), p.getY(), values));
        }
        float[] extend = {minx-50, miny-50, maxx+50, maxy+50};

        float dx = extend[2] - extend[0];
        float dy = extend[3] - extend[1];
        int[] size = {(int)(dx/100), (int)(dy/100)};

        String crs = "EPSG:25832";

        return new GridResponse(features, crs, extend, size);
    }
}
