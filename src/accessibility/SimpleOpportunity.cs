using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using DVAN.Routing;
using DVAN.Population;

namespace DVAN.Accessibility
{
    /// <summary>
    /// A simple opportunity based accessibility measure.
    /// Computes the aggregated opportunity (\sum{supply*decay}) at every population point.
    /// </summary>
    public class SimpleOpportunity
    {
        public static async Task<float[]> calcAccessibility(IPopulationView population, double[][] facilities, double[] capacities, List<double> ranges, IDistanceDecay decay, IRoutingProvider provider)
        {
            var accessibilities = new float[population.pointCount()];

            var matrix = await provider.requestTDMatrix(population, facilities, ranges, "isochrones");
            if (matrix == null) {
                return accessibilities;
            }

            for (int f = 0; f < facilities.Length; f++) {
                for (int p = 0; p < population.pointCount(); p++) {
                    float range = matrix.getRange(f, p);
                    if (range == 9999) {
                        continue;
                    }
                    float range_factor = decay.getDistanceWeight((float)range);

                    accessibilities[p] += (float)capacities[f] * (float)range_factor;
                }
            }

            float max_value = 0;
            for (int i = 0; i < accessibilities.Length; i++) {
                if (max_value == 0) {
                    max_value = accessibilities[i];
                }
                else {
                    if (max_value < accessibilities[i]) {
                        max_value = accessibilities[i];
                    }
                }
            }
            for (int key = 0; key < accessibilities.Length; key++) {
                var access = accessibilities[key];
                if (access == 0) {
                    accessibilities[key] = -9999;
                }
                else {
                    accessibilities[key] = access * 100 / max_value;
                }
            }

            return accessibilities;
        }
    }
}