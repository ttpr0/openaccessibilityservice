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

public class SimpleAccessibility {
    private Population population;
    private IRoutingProvider provider;
    private Accessibility accessibility;

    public SimpleAccessibility(Population population, IRoutingProvider provider) {
        this.population = population;
        this.provider = provider;
    }

    public void calcAccessibility(Double[][] facilities, List<Double> ranges) throws Exception {
        float[] population_weights = new float[population.getPointCount()];
        float[] facility_weights = new float[facilities.length];
        for (PopulationAttributes attr : population.attributes) {
            population_weights[attr.getIndex()] = attr.getPopulationCount();
        }
        PopulationAccessibility[] accessibilities = new PopulationAccessibility[population.getPointCount()];
        for (int i = 0; i < accessibilities.length; i++) {
            accessibilities[i] = new PopulationAccessibility();
        }

        FacilityCatchment[] catchments = new FacilityCatchment[facilities.length];
        for (int i = 0; i < catchments.length; i++) {
            catchments[i] = new FacilityCatchment();
        }
        Double[][] locations = new Double[1][2];
        for (int f=0; f<facilities.length; f++) {
            locations[0][0] = facilities[f][0];
            locations[0][1] = facilities[f][1];
            IsochroneCollection isochrones = provider.requestIsochrones(locations, ranges).get(0);

            for (int i=0; i< isochrones.getIsochronesCount(); i++) {
                Isochrone isochrone = isochrones.getIsochrone(i);
                double range = isochrone.getValue();
                Geometry iso;
                Geometry outer = isochrone.getGeometry();
                if (i==0) {
                    iso = outer;
                }
                else {
                    Geometry inner = isochrones.getIsochrone(i-1).getGeometry();
                    iso = outer.difference(inner);
                }
                Envelope env = iso.getEnvelopeInternal();
                List<PopulationPoint> points = population.getPointsInEnvelop(env);
                int population_count = 0;
                for (PopulationPoint p : points) {
                    if (p.getPoint().within(iso)) {
                        PopulationAttributes attr = p.getAttributes();
                        int index = attr.getIndex();

                        accessibilities[index].addRange((int)range);
                        population_count += attr.getPopulationCount();
                    }
                }
                catchments[f].addRangeRef(range, population_count);
            }
        }

        this.accessibility = new Accessibility(accessibilities, catchments);
    }

    public GridResponse buildResponse() {
        List<GridFeature> features = new ArrayList<GridFeature>();
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
            SimpleValue value = new SimpleValue(-9999, -9999, -9999);
            if (feature.ranges.size() > 0){
                value.first = feature.ranges.get(0);
            }
            if (feature.ranges.size() > 1){
                value.second = feature.ranges.get(1);
            }
            if (feature.ranges.size() > 2){
                value.third = feature.ranges.get(2);
            }
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

class RangeRef {
    double range;
    int count;

    public RangeRef(double range, int count) {
        this.range = range;
        this.count = count;
    }
}

class FacilityCatchment {
    List<RangeRef> population_counts;

    FacilityCatchment() {
        this.population_counts = new ArrayList<RangeRef>();
    }

    void addRangeRef(double range, int count) {
        this.population_counts.add(new RangeRef(range, count));
    }
}

class SimpleValue {
    public int first;
    public int second;
    public int third;

    public SimpleValue(int first, int second, int third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}