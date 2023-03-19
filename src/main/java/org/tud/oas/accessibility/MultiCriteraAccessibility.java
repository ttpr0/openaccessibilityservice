package org.tud.oas.accessibility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.tud.oas.api.accessibility.GridFeature;
import org.tud.oas.api.accessibility.GridResponse;
import org.tud.oas.population.Population;
import org.tud.oas.population.PopulationAttributes;
import org.tud.oas.population.PopulationView;

public class MultiCriteraAccessibility {
    private PopulationView population;
    private GravityAccessibility gravity;

    private float max_population;
    private float max_value;
    private float max_weighted_value;

    private Map<Integer, HashMap<String, Float>> accessibilities;

    public MultiCriteraAccessibility(PopulationView population, GravityAccessibility gravity) {
        this.population = population;
        this.gravity = gravity;

        float max_pop = 100;
        this.max_population = max_pop;

        this.accessibilities = new HashMap(10000);
    }

    public void addAccessibility(String name, Double[][] facilities, List<Double> ranges, List<Double> factors, double weight) throws Exception {
        gravity.calcAccessibility(facilities, ranges, factors);
        Map<Integer, Access> accessibility = gravity.getAccessibility();

        Access defaultAccess = new Access();
        defaultAccess.access = -9999;
        defaultAccess.weighted_access = -9999;
        for (Integer index : accessibility.keySet()) {
            Access access = accessibility.getOrDefault(index, defaultAccess);
            HashMap<String, Float> multi_access;
            if (!this.accessibilities.containsKey(index)) {
                this.accessibilities.put(index, new HashMap<String, Float>());
                multi_access = this.accessibilities.get(index);
                multi_access.put("multiCritera", 0.0f);
                multi_access.put("multiCritera_weighted", 0.0f);
            } else {
                multi_access = this.accessibilities.get(index);
            }
            multi_access.put(name, access.access);
            multi_access.put(name + "_weighted", access.weighted_access);
            if (access.access == -9999) {
                continue;
            }
            float temp = multi_access.get("multiCritera");
            float weighted_temp = multi_access.get("multiCritera_weighted");
            float new_value = temp + access.access;
            float new_weighted_value = weighted_temp + access.access * population.getPopulationCount(index) / max_population;
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
        for (Integer index : this.accessibilities.keySet()) {
            HashMap<String, Float> multi_access = this.accessibilities.get(index);
            float temp = multi_access.get("multiCritera");
            float weighted_temp = multi_access.get("multiCritera_weighted");
            if (temp == 0.0) {
                multi_access.put("multiCritera", -9999.0f);
                multi_access.put("multiCritera_weighted", -9999.0f);
            }
            else {
                multi_access.put("multiCritera", temp * 100 / max_value);
                multi_access.put("multiCritera_weighted", weighted_temp * 100 / max_weighted_value);
            }
        }
    }

    public GridResponse buildResponse() {
        List<GridFeature> features = new ArrayList<GridFeature>();
        float minx = 1000000000;
        float maxx = -1;
        float miny = 1000000000;
        float maxy = -1;
        List<Integer> indices = population.getAllPoints();
        for (int index : indices) {
            Coordinate p = population.getCoordinate(index, "EPSG:25832");
            HashMap<String, Float> values;
            if (this.accessibilities.containsKey(index)) {
                values = this.accessibilities.get(index);
            } else {
                values = new HashMap<String, Float>();
                values.put("multiCritera", -9999.0f);
                values.put("multiCritera_weighted", -9999.0f);
            }

            if (p.getX() < minx) {
                minx = (float)p.getX();
            }
            if (p.getX() > maxx) {
                maxx = (float)p.getX();
            }
            if (p.getY() < miny) {
                miny = (float)p.getY();
            }
            if (p.getY() > maxy) {
                maxy = (float)p.getY();
            }
            features.add(new GridFeature((float)p.getX(), (float)p.getY(), values));
        }
        float[] extend = {minx-50, miny-50, maxx+50, maxy+50};

        float dx = extend[2] - extend[0];
        float dy = extend[3] - extend[1];
        int[] size = {(int)(dx/100), (int)(dy/100)};

        String crs = "EPSG:25832";

        return new GridResponse(features, crs, extend, size);
    }
}
