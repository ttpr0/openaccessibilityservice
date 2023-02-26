package org.tud.oas.accessibility;

import org.tud.oas.api.accessibility.GridFeature;
import org.tud.oas.api.accessibility.GridResponse;
import org.tud.oas.population.Population;
import org.tud.oas.population.PopulationAttributes;
import org.tud.oas.population.PopulationPoint;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.IsochroneCollection;
import org.tud.oas.routing.Isochrone;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class GravityAccessibility {
    private Population population;
    private IRoutingProvider provider;
    private float[] accessibility;
    private float[] weighted_accessibility;

    public GravityAccessibility(Population population, IRoutingProvider provider) {
        this.population = population;
        this.provider = provider;
    }

    public void calcAccessibility(Double[][] facilities, List<Double> ranges, List<Double> factors) throws Exception {
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
        float[] accessibilities = new float[population.getPointCount()];
        float[] weighted_accessibilities = new float[population.getPointCount()];
        boolean[] visited = new boolean[population.getPointCount()];

        HashMap<Double, Geometry> polygons = new HashMap<Double, Geometry>(ranges.size());
        Double[][] locations = new Double[1][2];
        for (int f=0; f<facilities.length; f++) {
            locations[0][0] = facilities[f][0];
            locations[0][1] = facilities[f][1];
            IsochroneCollection isochrones = provider.requestIsochrones(locations, ranges).get(0);

            for (int i=0; i< isochrones.getIsochronesCount(); i++) {
                Isochrone isochrone = isochrones.getIsochrone(i);
                double range = isochrone.getValue();
                
                if (!polygons.containsKey(range)) {
                    polygons.put(range, isochrone.getGeometry());
                }
                else {
                    Geometry geometry = polygons.get(range);
                    polygons.put(range, geometry.union(isochrone.getGeometry()));
                }
            }
        }

        float max_value = 0;
        for (int i=0; i<ranges.size(); i++) {
            double range = ranges.get(i);
            double factor = factors.get(i);
            Geometry iso = polygons.get(range);

            Envelope env = iso.getEnvelopeInternal();
            List<PopulationPoint> points = population.getPointsInEnvelop(env);

            for (PopulationPoint p : points) {
                PopulationAttributes attr = p.getAttributes();
                int index = attr.getIndex();
                if (visited[index]) {
                    continue;
                }
                if (p.getPoint().within(iso)) {
                    accessibilities[index] += factor / range;
                    if (accessibilities[index] > max_value) {
                        max_value = accessibilities[index];
                    }
                }
            }
        }

        for (int i=0; i<accessibilities.length; i++) {
            if (accessibilities[i] == 0) {
                accessibilities[i] = -9999;
                weighted_accessibilities[i] = -9999;
            }
            else {
                accessibilities[i] = accessibilities[i] * 100 / max_value;
                weighted_accessibilities[i] = accessibilities[i] * population_weights[i] / max_pop;
            }
        }
        this.accessibility = accessibilities;
        this.weighted_accessibility = weighted_accessibilities;
    }

    public GridResponse buildResponse() {
        List<GridFeature> features = new ArrayList<GridFeature>();
        float minx = 1000000000;
        float maxx = -1;
        float miny = 1000000000;
        float maxy = -1;
        for (int i=0; i< population.getPointCount(); i++) {
            PopulationPoint p = population.getPoint(i);
            float access = this.accessibility[i];
            float weighted_access = this.weighted_accessibility[i];
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
            GravityValue value = new GravityValue(access, weighted_access);
            features.add(new GridFeature(p.getX(), p.getY(), value));
        }
        float[] extend = {minx-50, miny-50, maxx+50, maxy+50};

        float dx = extend[2] - extend[0];
        float dy = extend[3] - extend[1];
        int[] size = {(int)(dx/100), (int)(dy/100)};

        String crs = "EPSG:25832";

        return new GridResponse(features, crs, extend, size);
    }
}

class GravityValue {
    public float unweighted;
    public float weighted;

    public GravityValue(float unweighted, float weighted) {
        this.unweighted = unweighted;
        this.weighted = weighted;
    }
}