using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;
using NetTopologySuite.Algorithm.Locate;
using System.Threading.Tasks;
using DVAN.Routing;
using DVAN.Population;

namespace DVAN.FCA
{
    public class Simple2SFCA 
    {
        public static async Task<float[]> calc2SFCA(PopulationContainer population, Double[][] facilities, List<Double> ranges, List<Double> range_factors, IRoutingProvider provider)
        {
            float[] population_weights = new float[population.getPointCount()];
            float[] facility_weights = new float[facilities.Length];
            foreach (var attr in population.attributes) {
                population_weights[attr.getIndex()] = attr.getPopulationCount();
            }

            float max_range = (float)ranges[ranges.Count-1];

            var inverted_mapping = new List<FacilityReference>[population.getPointCount()];
            Double[][] locations = new Double[1][];
            for (int f=0; f<facilities.Length; f++) {
                locations[0][0] = facilities[f][0];
                locations[0][1] = facilities[f][1];
                var isochrones = (await provider.requestIsochrones(locations, ranges))[0];

                float weight = 0;
                for (int i=0; i< isochrones.getIsochronesCount(); i++) {
                    var isochrone = isochrones.getIsochrone(i);
                    double range = isochrone.getValue();
                    double range_factor = range_factors[ranges.IndexOf(range)];
                    Geometry iso;
                    Geometry outer = isochrone.getGeometry();
                    if (i==0) {
                        iso = outer;
                    }
                    else {
                        Geometry inner = isochrones.getIsochrone(i-1).getGeometry();
                        iso = outer.Difference(inner);
                    }
                    Envelope env = iso.EnvelopeInternal;
                    List<int> points = population.getPointsInEnvelop(env);
                    foreach (int index in points) {
                        Coordinate p = population.getPoint(index);
                        var location = SimplePointInAreaLocator.Locate(p, iso);
                        if (location == Location.Interior) {
                            weight += population_weights[index] * (float)range_factor;

                            if (inverted_mapping[index] == null) {
                                inverted_mapping[index] = new List<FacilityReference>(4);
                            }
                            inverted_mapping[index].Add(new FacilityReference(f, (float)range));
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
                    foreach (FacilityReference fref in refs) {
                        double range_factor = range_factors[ranges.IndexOf((double)fref.range)];
                        weight += (float)(facility_weights[fref.index] * range_factor);
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
}