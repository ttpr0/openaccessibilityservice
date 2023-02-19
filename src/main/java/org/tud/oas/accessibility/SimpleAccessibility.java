package org.tud.oas.accessibility;

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

    public static Accessibility calcAccessibility(Population population, Double[][] facilities, List<Double> ranges, IRoutingProvider provider) throws Exception {
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

        return new Accessibility(accessibilities, catchments);
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