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
    public class SimpleCatchment
    {
        private IPopulationView population;
        private IRoutingProvider provider;
        private Dictionary<int, List<int>>? accessibilities;

        public SimpleCatchment(IPopulationView population, IRoutingProvider provider)
        {
            this.population = population;
            this.provider = provider;
        }

        public Dictionary<int, List<int>>? getAccessibilities()
        {
            return this.accessibilities;
        }

        public async Task calcAccessibility(double[][] facilities, double catchment_range)
        {
            var accessibilities = new Dictionary<int, List<int>>(10000);

            var collection = provider.requestIsochronesStream(facilities, new List<double> { catchment_range });
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
                    foreach (int index in points) {
                        Coordinate p = population.getCoordinate(index);
                        var location = SimplePointInAreaLocator.Locate(p, iso);
                        if (location == Location.Interior) {
                            List<int> access;
                            if (!accessibilities.ContainsKey(index)) {
                                access = new List<int>();
                                accessibilities[index] = access;
                            }
                            else {
                                access = accessibilities[index];
                            }
                            accessibilities[index].Add(facility_index);
                        }
                    }
                }
            }

            this.accessibilities = accessibilities;
        }
    }
}