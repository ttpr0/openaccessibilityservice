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
    public class Enhanced2SFCA
    {

        public static Task<float[]> calc2SFCA(IPopulationView population, double[][] facilities, List<double> ranges, List<double> range_factors, IRoutingProvider provider, string? mode)
        {
            switch (mode) {
                case "isochrones":
                    return calc2SFCAIsochrones(population, facilities, ranges, range_factors, provider);
                case "matrix":
                    return calc2SFCAMatrix(population, facilities, ranges, range_factors, provider);
                case "isoraster":
                    return calc2SFCAIsoRaster(population, facilities, ranges, range_factors, provider);
                default:
                    return calc2SFCAIsochrones(population, facilities, ranges, range_factors, provider);
            }
        }

        public static async Task<float[]> calc2SFCAIsochrones(IPopulationView population, double[][] facilities, List<double> ranges, List<double> range_factors, IRoutingProvider provider)
        {
            var population_weights = new float[population.pointCount()];
            var facility_weights = new float[facilities.Length];

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
                            int population_count = population.getPopulation(index);
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

        public static async Task<float[]> calc2SFCAMatrix(IPopulationView population, double[][] facilities, List<double> ranges, List<double> range_factors, IRoutingProvider provider)
        {
            var population_weights = new float[population.pointCount()];
            float[] facility_weights = new float[facilities.Length];

            float max_range = (float)ranges[^1];
            var inverted_mapping = new Dictionary<int, List<FacilityReference>>();

            var point_count = population.pointCount();
            double[][] destinations = new double[point_count][];
            for (int i = 0; i < point_count; i++) {
                var index = i;
                Coordinate p = population.getCoordinate(index);
                destinations[i] = new double[] { p.X, p.Y };
            }
            var matrix = await provider.requestMatrix(facilities, destinations);
            if (matrix == null || matrix.durations == null) {
                return population_weights;
            }
            for (int f = 0; f < facilities.Length; f++) {
                float weight = 0;
                for (int i = 0; i < point_count; i++) {
                    float range = (float)matrix.durations[f][i];
                    if (range > max_range) {
                        continue;
                    }
                    int index = i;
                    int population_count = population.getPopulation(index);
                    float range_factor = 1 - range / max_range;

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
                        double range_factor = 1 - fref.range / max_range;
                        weight += (float)(facility_weights[fref.index] * range_factor);
                    }
                    population_weights[index] = weight;
                }
            }

            return population_weights;
        }

        public static async Task<float[]> calc2SFCAIsoRaster(IPopulationView population, double[][] facilities, List<double> ranges, List<double> range_factors, IRoutingProvider provider)
        {
            var population_weights = new float[population.pointCount()];
            float[] facility_weights = new float[facilities.Length];

            float max_range = (float)ranges[^1];
            var inverted_mapping = new Dictionary<int, List<FacilityReference>>();

            var isoraster = await provider.requestIsoRaster(facilities, max_range);
            if (isoraster == null) {
                return population_weights;
            }

            double[][] extend = isoraster.getEnvelope();
            var env = new Envelope(extend[0][0], extend[3][0], extend[2][1], extend[1][1]);
            var points = population.getPointsInEnvelop(env);
            foreach (int index in points) {
                Coordinate p = population.getCoordinate(index, "EPSG:25832");
                var accessor = isoraster.getAccessor(p);
                if (accessor != null) {
                    foreach (var f in accessor.getFacilities()) {
                        float range = accessor.getRange(f);

                        int population_count = population.getPopulation(index);
                        float range_factor = 1 - range / max_range;
                        facility_weights[f] += population_count * range_factor;

                        if (!inverted_mapping.ContainsKey(index)) {
                            inverted_mapping[index] = new List<FacilityReference>(4);
                        }
                        inverted_mapping[index].Add(new FacilityReference(f, range));
                    }
                }
            }
            for (int f = 0; f < facility_weights.Length; f++) {
                float weight = facility_weights[f];
                if (weight != 0) {
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
                        double range_factor = 1 - fref.range / max_range; ;
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