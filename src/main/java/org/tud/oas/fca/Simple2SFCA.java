package org.tud.oas.fca;

import org.tud.oas.population.Population;
import org.tud.oas.population.PopulationAttributes;
import org.tud.oas.routing.IsochroneCollection;
import org.tud.oas.routing.Isochrone;
import org.tud.oas.routing.IRoutingProvider;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.algorithm.locate.SimplePointInAreaLocator;

import java.util.List;
import java.util.ArrayList;

public class Simple2SFCA {
    public static float[] calc2SFCA(Population population, Double[][] facilities, List<Double> ranges, List<Double> range_factors, IRoutingProvider provider) throws Exception {
        float[] population_weights = new float[population.getPointCount()];
        float[] facility_weights = new float[facilities.length];
        for (PopulationAttributes attr : population.attributes) {
            population_weights[attr.getIndex()] = attr.getPopulationCount();
        }

        float max_range = ranges.get(ranges.size()-1).floatValue();

        ArrayList<FacilityReference>[] inverted_mapping = new ArrayList[population.getPointCount()];
        Double[][] locations = new Double[1][2];
        for (int f=0; f<facilities.length; f++) {
            locations[0][0] = facilities[f][0];
            locations[0][1] = facilities[f][1];
            IsochroneCollection isochrones = provider.requestIsochrones(locations, ranges).get(0);

            float weight = 0;
            for (int i=0; i< isochrones.getIsochronesCount(); i++) {
                Isochrone isochrone = isochrones.getIsochrone(i);
                double range = isochrone.getValue();
                double range_factor = range_factors.get(ranges.indexOf(range));
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
                List<Integer> points = population.getPointsInEnvelop(env);
                for (Integer index : points) {
                    Coordinate p = population.getPoint(index);
                    int location = SimplePointInAreaLocator.locate(p, iso);
                    if (location == Location.INTERIOR) {
                        weight += population_weights[index] * range_factor;

                        if (inverted_mapping[index] == null) {
                            inverted_mapping[index] = new ArrayList<FacilityReference>(4);
                        }
                        inverted_mapping[index].add(new FacilityReference(f, (float)range));
                    }
                }
            }
            if (weight == 0) {
                facility_weights[f] = 0;
            }
            else {
                facility_weights[f] = 1/weight;
            }
        }

        for (int p=0; p<population.getPointCount(); p++) {
            List<FacilityReference> refs = inverted_mapping[p];
            float weight = -1;
            if (refs == null) {
                population_weights[p] = weight;
            }
            else {
                weight = 0;
                for (FacilityReference ref : refs) {
                    double range_factor = range_factors.get(ranges.indexOf((double)ref.range));
                    weight += (float)(facility_weights[ref.index] * range_factor);
                }
                population_weights[p] = weight;
            }
        }

        return population_weights;
    }
}

class FacilityReference {
    public int index;
    public float range;

    public FacilityReference(int index, float range) {
        this.index = index;
        this.range = range;
    }
}