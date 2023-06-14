using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using DVAN.API;
using DVAN.Routing;
using DVAN.Population;
using NetTopologySuite.Algorithm.Locate;
using NetTopologySuite.Geometries;
using NetTopologySuite.Simplify;
using System.Threading.Tasks.Dataflow;

namespace DVAN.Accessibility
{
    public class Access
    {
        public float access;
        public float weighted_access;
    }

    public class GravityAccessibility
    {
        private IPopulationView population;
        private IRoutingProvider provider;

        private float max_population;
        private Access[] accessibility;

        public GravityAccessibility(IPopulationView population, IRoutingProvider provider)
        {
            this.population = population;
            this.provider = provider;

            this.max_population = 100;
            this.accessibility = new Access[population.pointCount()];
        }

        public Access[] getAccessibility()
        {
            return this.accessibility;
        }

        public async Task calcAccessibility(double[][] facilities, List<double> ranges, List<double> factors)
        {
            var accessibilities = new Access[this.population.pointCount()];

            var table = await this.provider.requestNearest(this.population, facilities, ranges, "isochrones");
            if (table == null) {
                return;
            }

            float max_value = 0;
            for (int p = 0; p < this.population.pointCount(); p++) {
                var (_, range) = table.getNearest(p);
                var factor = factors[ranges.IndexOf(range)];

                Access access;
                if (accessibilities[p] == null) {
                    access = new Access();
                    accessibilities[p] = access;
                }
                else {
                    access = accessibilities[p];
                }
                accessibilities[p].access += (float)factor;
                if (access.access > max_value) {
                    max_value = access.access;
                }
            }

            for (int key = 0; key < accessibilities.Length; key++) {
                var access = accessibilities[key];
                if (access.access == 0) {
                    access.access = -9999;
                    access.weighted_access = -9999;
                }
                else {
                    access.access = access.access * 100 / max_value;
                    access.weighted_access = access.access * this.population.getPopulation(key) / max_population;
                }
            }
            this.accessibility = accessibilities;
        }

        // public async Task calcAccessibility2(double[][] facilities, List<double> ranges, List<double> factors)
        // {
        //     var accessibilities = new Access[this.population.pointCount()];

        //     ISourceBlock<IsoRaster?> collection = provider.requestIsoRasterStream(facilities, ranges[ranges.Count - 1]);

        //     float max_value = 0;
        //     for (int j = 0; j < facilities.Length; j++) {
        //         var raster = await collection.ReceiveAsync();
        //         if (raster == null) {
        //             continue;
        //         }
        //         double[][] extend = raster.getEnvelope();
        //         Envelope env = new Envelope(extend[0][0], extend[3][0], extend[2][1], extend[1][1]);
        //         List<int> points = population.getPointsInEnvelop(env);

        //         long start = Environment.TickCount64;
        //         foreach (int index in points) {
        //             Coordinate p = population.getCoordinate(index, "EPSG:25832");
        //             var range_dict = raster.getValueAtCoordinate(p);
        //             if (range_dict == null) {
        //                 continue;
        //             }
        //             bool found = range_dict.TryGetValue(0, out int range);
        //             if (found) {
        //                 Access access;
        //                 if (accessibilities[index] == null) {
        //                     access = new Access();
        //                     accessibilities[index] = access;
        //                 }
        //                 else {
        //                     access = accessibilities[index];
        //                 }
        //                 for (int i = 0; i < ranges.Count; i++) {
        //                     if (range <= ranges[i]) {
        //                         access.access += (float)factors[i];
        //                         if (access.access > max_value) {
        //                             max_value = access.access;
        //                         }
        //                         break;
        //                     }
        //                 }
        //             }
        //         }
        //         long end = Environment.TickCount64;
        //         Console.WriteLine("time: " + (end - start));
        //     }

        //     for (int index = 0; index < accessibilities.Length; index++) {
        //         Access access = accessibilities[index];
        //         if (access.access == 0) {
        //             access.access = -9999;
        //             access.weighted_access = -9999;
        //         }
        //         else {
        //             access.access = access.access * 100 / max_value;
        //             access.weighted_access = access.access * this.population.getPopulation(index) / max_population;
        //         }
        //     }
        //     this.accessibility = accessibilities;
        // }
    }
}