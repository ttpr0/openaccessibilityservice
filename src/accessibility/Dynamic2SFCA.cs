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

            // calculating supply-demand-ratio by using time-distance-matrix
            var matrix = await provider.requestTDMatrix(population, facilities, ranges, "isochrones");
            if (matrix == null) {
                return population_weights;
            }
            for (int f = 0; f < facilities.Length; f++) {
                float weight = 0;
                for (int p = 0; p < population.pointCount(); p++) {
                    float range = matrix.getRange(f, p);
                    if (range == 9999) {
                        continue;
                    }
                    int range_index = ranges.IndexOf(range);
                    if (range_index != range_indizes[p]) {
                        continue;
                    }
                    int population_count = population.getPopulation(p);
                    weight += population_count;

                    if (!inverted_mapping.ContainsKey(p)) {
                        inverted_mapping[p] = new List<int>(4);
                    }
                    inverted_mapping[p].Add(f);
                }
                if (weight == 0) {
                    facility_weights[f] = 0;
                }
                else {
                    facility_weights[f] = (float)capacities[f] / weight;
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
