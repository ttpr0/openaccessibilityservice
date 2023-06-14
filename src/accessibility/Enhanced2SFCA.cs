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
    /// <summary>
    /// Computes Enhanced 2SFCA using 3 different calculation modes.
    /// Result is an array containing a access value for every population point.
    /// </summary>
    public class Enhanced2SFCA
    {

        public static Task<float[]> calc2SFCA(IPopulationView population, double[][] facilities, double[] capacities, List<double> ranges, IDistanceDecay decay, IRoutingProvider provider, string? mode)
        {
            switch (mode) {
                case "isochrones":
                    return calc2SFCAIsochrones(population, facilities, capacities, ranges, decay, provider);
                case "matrix":
                    return calc2SFCAMatrix(population, facilities, capacities, ranges, decay, provider);
                case "isoraster":
                    return calc2SFCAIsoRaster(population, facilities, capacities, ranges, decay, provider);
                default:
                    return calc2SFCAIsochrones(population, facilities, capacities, ranges, decay, provider);
            }
        }

        public static async Task<float[]> calc2SFCAIsochrones(IPopulationView population, double[][] facilities, double[] capacities, List<double> ranges, IDistanceDecay decay, IRoutingProvider provider)
        {
            var population_weights = new float[population.pointCount()];
            var facility_weights = new float[facilities.Length];

            var inverted_mapping = new Dictionary<int, List<FacilityReference>>();

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
                    float range_factor = decay.getDistanceWeight((float)range);
                    int population_count = population.getPopulation(p);
                    weight += population_count * (float)range_factor;

                    if (!inverted_mapping.ContainsKey(p)) {
                        inverted_mapping[p] = new List<FacilityReference>(4);
                    }
                    inverted_mapping[p].Add(new FacilityReference(f, (float)range));
                }
                if (weight == 0) {
                    facility_weights[f] = 0;
                }
                else {
                    facility_weights[f] = (float)capacities[f] / weight;
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
                        double range_factor = decay.getDistanceWeight(fref.range);
                        weight += (float)(facility_weights[fref.index] * range_factor);
                    }
                    population_weights[index] = weight;
                }
            }

            return population_weights;
        }

        public static async Task<float[]> calc2SFCAMatrix(IPopulationView population, double[][] facilities, double[] capacities, List<double> ranges, IDistanceDecay decay, IRoutingProvider provider)
        {
            var population_weights = new float[population.pointCount()];
            float[] facility_weights = new float[facilities.Length];

            float max_range = (float)ranges[^1];
            var inverted_mapping = new Dictionary<int, List<FacilityReference>>();

            var point_count = population.pointCount();
            var matrix = await provider.requestTDMatrix(population, facilities, ranges, "matrix");
            if (matrix == null) {
                return population_weights;
            }
            for (int f = 0; f < facilities.Length; f++) {
                float weight = 0;
                for (int i = 0; i < point_count; i++) {
                    float range = matrix.getRange(f, i);
                    if (range > max_range) {
                        continue;
                    }
                    int index = i;
                    int population_count = population.getPopulation(index);
                    float range_factor = decay.getDistanceWeight(range);

                    weight += population_count * range_factor;

                    if (!inverted_mapping.ContainsKey(index)) {
                        inverted_mapping[index] = new List<FacilityReference>(4);
                    }
                    inverted_mapping[index].Add(new FacilityReference(f, range));
                }
                if (weight == 0) {
                    facility_weights[f] = 0;
                }
                else {
                    facility_weights[f] = (float)capacities[f] / weight;
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
                        double range_factor = decay.getDistanceWeight(fref.range);
                        weight += (float)(facility_weights[fref.index] * range_factor);
                    }
                    population_weights[index] = weight;
                }
            }

            return population_weights;
        }

        public static async Task<float[]> calc2SFCAIsoRaster(IPopulationView population, double[][] facilities, double[] capacities, List<double> ranges, IDistanceDecay decay, IRoutingProvider provider)
        {
            var population_weights = new float[population.pointCount()];
            var facility_weights = new float[facilities.Length];

            var inverted_mapping = new Dictionary<int, List<FacilityReference>>();

            var matrix = await provider.requestTDMatrix(population, facilities, ranges, "isoraster");
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
                    float range_factor = decay.getDistanceWeight((float)range);
                    int population_count = population.getPopulation(p);
                    weight += population_count * (float)range_factor;

                    if (!inverted_mapping.ContainsKey(p)) {
                        inverted_mapping[p] = new List<FacilityReference>(4);
                    }
                    inverted_mapping[p].Add(new FacilityReference(f, (float)range));
                }
                if (weight == 0) {
                    facility_weights[f] = 0;
                }
                else {
                    facility_weights[f] = (float)capacities[f] / weight;
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
                        double range_factor = decay.getDistanceWeight(fref.range);
                        weight += (float)(facility_weights[fref.index] * range_factor);
                    }
                    population_weights[index] = weight;
                }
            }

            return population_weights;
        }
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