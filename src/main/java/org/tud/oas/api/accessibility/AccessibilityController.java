package org.tud.oas.api.accessibility;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tud.oas.accessibility.Accessibility;
import org.tud.oas.accessibility.PopulationAccessibility;
import org.tud.oas.accessibility.SimpleAccessibility;
import org.tud.oas.population.Population;
import org.tud.oas.population.PopulationManager;
import org.tud.oas.population.PopulationPoint;

@RestController
@RequestMapping("/v1/accessibility")
public class AccessibilityController {

    @PostMapping
    public GridResponse calculateFCA(@RequestBody AccessibilityRequest request) throws Exception {
        Population population = PopulationManager.getPopulation();

		Accessibility accessibility = SimpleAccessibility.calcAccessibility(population, request.getLocations(), request.getRanges());

        List<Feature> features = new ArrayList<Feature>();
        float minx = 1000000000;
        float maxx = -1;
        float miny = 1000000000;
        float maxy = -1;
        for (int i=0; i< population.getPointCount(); i++) {
            PopulationPoint p = population.getPoint(i);
            PopulationAccessibility feature = accessibility.accessibilities[i];
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
            feature.ranges.sort((Integer a, Integer b) -> {
                return a - b;
            });
            AccessibilityValue value = new AccessibilityValue(-9999, -9999, -9999);
            if (feature.ranges.size() > 0){
                value.first = feature.ranges.get(0);
            }
            if (feature.ranges.size() > 1){
                value.second = feature.ranges.get(1);
            }
            if (feature.ranges.size() > 2){
                value.third = feature.ranges.get(2);
            }
            features.add(new Feature(p.getX(), p.getY(), value));
        }
        float[] extend = {minx-50, miny-50, maxx+50, maxy+50};

        float dx = extend[2] - extend[0];
        float dy = extend[3] - extend[1];
        int[] size = {(int)(dx/100), (int)(dy/100)};

        String crs = "EPSG:25832";

        return new GridResponse(features, crs, extend, size);
    }
}

class AccessibilityValue {
    public int first;
    public int second;
    public int third;

    public AccessibilityValue(int first, int second, int third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}