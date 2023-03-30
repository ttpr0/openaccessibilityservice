using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;
using NetTopologySuite.Algorithm.Locate;
using System.Threading.Tasks;
using DVAN.Routing;
using DVAN.Population;
using System.Threading.Tasks.Dataflow;

namespace DVAN.FCA
{
    public class Simple2SFCA
    {
        public static async Task<Dictionary<int, float>> calc2SFCA(PopulationView population, double[][] facilities, List<double> ranges, List<double> range_factors, IRoutingProvider provider)
        {
            var population_weights = new Dictionary<int, float>();
            float[] facility_weights = new float[facilities.Length];

            var inverted_mapping = new Dictionary<int, List<FacilityReference>>();

            var collection = provider.requestIsochronesStream(facilities, ranges);
            for (int f = 0; f < facilities.Length; f++) {
                var isochrones = await collection.ReceiveAsync();
                if (isochrones == null) {
                    continue;
                }

                var visited = new HashSet<int>(10000);
                float weight = 0;
                for (int i = 0; i < isochrones.getIsochronesCount(); i++) {
                    var isochrone = isochrones.getIsochrone(i);
                    double range = isochrone.getValue();
                    double range_factor = range_factors[ranges.IndexOf(range)];

                    Geometry iso = isochrone.getGeometry();
                    Envelope env = iso.EnvelopeInternal;

                    List<int> points = population.getPointsInEnvelop(env);
                    foreach (int index in points) {
                        if (visited.Contains(index)) {
                            continue;
                        }
                        Coordinate p = population.getCoordinate(index);
                        var location = SimplePointInAreaLocator.Locate(p, iso);
                        if (location == Location.Interior) {
                            int population_count = population.getPopulationCount(index);
                            weight += population_count * (float)range_factor;

                            if (!inverted_mapping.ContainsKey(index)) {
                                inverted_mapping[index] = new List<FacilityReference>(4);
                            }
                            inverted_mapping[index].Add(new FacilityReference(f, (float)range));
                            visited.Add(index);
                        }
                    }
                }
                if (weight == 0) {
                    facility_weights[f] = 0;
                }
                else {
                    facility_weights[f] = 1 / weight;
                }
            }

            foreach (int index in inverted_mapping.Keys) {
                List<FacilityReference> refs = inverted_mapping[index];
                if (refs == null) {
                    continue;
                }
                else {
                    float weight = 0;
                    foreach (FacilityReference fref in refs) {
                        double range_factor = range_factors[ranges.IndexOf(fref.range)];
                        weight += (float)(facility_weights[fref.index] * range_factor);
                    }
                    population_weights[index] = weight;
                }
            }

            return population_weights;
        }
    }

    class FacilityReference
    {
        public int index;
        public float range;

        public FacilityReference(int index, float range)
        {
            this.index = index;
            this.range = range;
        }
    }
}