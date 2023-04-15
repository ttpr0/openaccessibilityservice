using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using DVAN.API;
using DVAN.Routing;
using DVAN.Population;
using NetTopologySuite.Algorithm.Locate;
using NetTopologySuite.Geometries;
using System.Threading.Tasks.Dataflow;

namespace DVAN.Accessibility
{
    public class SimpleAccessibility
    {
        private IPopulationView population;
        private IRoutingProvider provider;
        private Dictionary<int, List<RangeRef>> accessibilities;
        private FacilityCatchment[] catchments;

        public SimpleAccessibility(IPopulationView population, IRoutingProvider provider)
        {
            this.population = population;
            this.provider = provider;
        }

        public Dictionary<int, List<RangeRef>> getAccessibilities()
        {
            return this.accessibilities;
        }

        public async Task calcAccessibility(Double[][] facilities, List<Double> ranges)
        {
            var accessibilities = new Dictionary<int, List<RangeRef>>(10000);

            FacilityCatchment[] catchments = new FacilityCatchment[facilities.Length];
            for (int i = 0; i < catchments.Length; i++) {
                catchments[i] = new FacilityCatchment();
            }

            var collection = provider.requestIsochronesStream(facilities, ranges);
            for (int f = 0; f < facilities.Length; f++) {
                var isochrones = await collection.ReceiveAsync();
                if (isochrones == null) {
                    continue;
                }
                int facility_index = isochrones.getID();

                // double[][] locations = new double[1][];
                // locations[0] = new double[] { 0, 0 };
                // for (int f = 0; f < facilities.Length; f++) {
                //     locations[0][0] = facilities[f][0];
                //     locations[0][1] = facilities[f][1];

                //     var iso_list = await provider.requestIsochrones(locations, ranges);
                //     if (iso_list == null) {
                //         continue;
                //     }
                //     var isochrones = iso_list[0];

                for (int i = 0; i < isochrones.getIsochronesCount(); i++) {
                    Isochrone isochrone = isochrones.getIsochrone(i);
                    double range = isochrone.getValue();
                    Geometry iso;
                    Geometry outer = isochrone.getGeometry();
                    if (i == 0) {
                        iso = outer;
                    }
                    else {
                        Geometry inner = isochrones.getIsochrone(i - 1).getGeometry();
                        iso = outer.Difference(inner);
                    }
                    Envelope env = iso.EnvelopeInternal;
                    List<int> points = population.getPointsInEnvelop(env);
                    int population_count = 0;
                    foreach (int index in points) {
                        Coordinate p = population.getCoordinate(index);
                        var location = SimplePointInAreaLocator.Locate(p, iso);
                        if (location == Location.Interior) {
                            List<RangeRef> access;
                            if (!accessibilities.ContainsKey(index)) {
                                access = new List<RangeRef>();
                                accessibilities[index] = access;
                            }
                            else {
                                access = accessibilities[index];
                            }
                            accessibilities[index].Add(new RangeRef((int)range, facility_index));
                            population_count += population.getPopulationCount(index);
                        }
                    }
                    catchments[f].addRangeRef(range, population_count);
                }
            }

            this.accessibilities = accessibilities;
            this.catchments = catchments;
        }
    }

    public struct RangeRef
    {
        public double range;
        public int index;

        public RangeRef(double range, int index)
        {
            this.range = range;
            this.index = index;
        }
    }

    public class FacilityCatchment
    {
        List<RangeRef> population_counts;

        public FacilityCatchment()
        {
            this.population_counts = new List<RangeRef>();
        }

        public void addRangeRef(double range, int count)
        {
            this.population_counts.Add(new RangeRef(range, count));
        }
    }
}