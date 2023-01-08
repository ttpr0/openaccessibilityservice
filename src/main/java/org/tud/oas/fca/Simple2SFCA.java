package org.tud.oas.fca;

import org.tud.oas.ors.Isochrones;
import org.tud.oas.population.Population;
import org.tud.oas.population.PopulationAttributes;
import org.tud.oas.population.PopulationPoint;
import org.heigit.ors.isochrones.Isochrone;
import org.heigit.ors.isochrones.IsochroneMap;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import java.util.List;
import java.util.ArrayList;

public class Simple2SFCA {
    public static float[] calc2SFCA(Population population, Double[][] facilities, List<Double> ranges, List<Double> range_factors) throws Exception {
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
            IsochroneMap isochrones = Isochrones.requestIsochrones(locations, ranges).getIsochrone(0);

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
                List<PopulationPoint> points = population.getPointsInEnvelop(env);
                for (PopulationPoint p : points) {
                    if (p.getPoint().within(iso)) {
                        int index = p.getAttributes().getIndex();
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