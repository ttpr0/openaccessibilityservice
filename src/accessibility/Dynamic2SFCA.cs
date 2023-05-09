using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;
using NetTopologySuite.Algorithm.Locate;
using System.Threading.Tasks;
using DVAN.Routing;
using DVAN.Population;
using System.Threading.Tasks.Dataflow;

namespace DVAN.Accessibility
{
    public class Dynamic2SFCA
    {
        public static async Task<float[]> calc2SFCA(IPopulationView population, int[] range_indizes, double[][] facilities, double[] capacities, List<double> ranges, IRoutingProvider provider)
        {
            // initialize arrays to store weights
            var population_weights = new float[population.pointCount()];
            var facility_weights = new float[facilities.Length];

            // inverted mapping (population -> facilities) to avoid recomputing catchments
            var inverted_mapping = new Dictionary<int, List<int>>();

            // calculating supply-demand-ratio by using isochrones and checking for point-in-area location
            var collection = provider.requestIsochronesStream(facilities, ranges);
            for (int f = 0; f < facilities.Length; f++) {
                var isochrones = await collection.ReceiveAsync();
                if (isochrones == null) {
                    continue;
                }
                int facility_index = isochrones.getID();

                float weight = 0;
                for (int i = 0; i < isochrones.getIsochronesCount(); i++) {
                    var isochrone = isochrones.getIsochrone(i);

                    // range_index used to identify catchment
                    int range_index = ranges.IndexOf(isochrone.getValue());

                    Geometry iso = isochrone.getGeometry();
                    Envelope env = iso.EnvelopeInternal;

                    List<int> points = population.getPointsInEnvelop(env);
                    foreach (int index in points) {
                        // check for right catchment on population point
                        if (range_index != range_indizes[index]) {
                            continue;
                        }
                        Coordinate p = population.getCoordinate(index);
                        var location = SimplePointInAreaLocator.Locate(p, iso);
                        if (location == Location.Interior) {
                            int population_count = population.getPopulation(index);
                            weight += population_count;

                            if (!inverted_mapping.ContainsKey(index)) {
                                inverted_mapping[index] = new List<int>(4);
                            }
                            inverted_mapping[index].Add(facility_index);
                        }
                    }
                }
                if (weight == 0) {
                    facility_weights[facility_index] = 0;
                }
                else {
                    facility_weights[facility_index] = (float)capacities[facility_index] / weight;
                }
            }

            // using inverted index to sum up supply-demand-ratios of facilities on population points without recomputing isochrones and point-in-are
            foreach (int index in inverted_mapping.Keys) {
                List<int> refs = inverted_mapping[index];
                if (refs == null) {
                    continue;
                }
                else {
                    float weight = 0;
                    foreach (int fref in refs) {
                        weight += facility_weights[fref];
                    }
                    population_weights[index] = weight;
                }
            }

            // return summed up supply-demand-ratios
            return population_weights;
        }
    }
}
