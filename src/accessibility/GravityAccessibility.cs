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
            var visited = new HashSet<int>(10000);
            var accessibilities = new Access[this.population.pointCount()];

            var polygons = new Dictionary<double, Geometry>(ranges.Count);

            // Double[][] locations = new Double[1][2];
            // for (int f=0; f<facilities.length; f++) {
            //     locations[0][0] = facilities[f][0];
            //     locations[0][1] = facilities[f][1];
            //     List<IsochroneCollection> isochrones_coll = provider.requestIsochrones(locations, ranges);
            //     if (isochrones_coll == null) {
            //         continue;
            //     }
            //     IsochroneCollection isochrones = isochrones_coll.get(0);

            var collection = provider.requestIsochronesStream(facilities, ranges);
            for (int j = 0; j < facilities.Length; j++) {
                var isochrones = await collection.ReceiveAsync();
                if (isochrones == null) {
                    continue;
                }

                for (int i = 0; i < isochrones.getIsochronesCount(); i++) {
                    Isochrone isochrone = isochrones.getIsochrone(i);
                    double range = isochrone.getValue();

                    if (!polygons.ContainsKey(range)) {
                        polygons[range] = isochrone.getGeometry();
                    }
                    else {
                        Geometry geometry = polygons[range];
                        Geometry union = geometry.Union(isochrone.getGeometry());
                        Geometry geom = new PolygonHullSimplifier(union, false).GetResult();
                        polygons[range] = geom;
                    }
                }
            }

            float max_value = 0;
            for (int i = 0; i < ranges.Count; i++) {
                double range = ranges[i];
                double factor = factors[i];
                Geometry iso = polygons[range];

                Envelope env = iso.EnvelopeInternal;
                List<int> points = population.getPointsInEnvelop(env);

                Geometry geom = new PolygonHullSimplifier(iso, false).GetResult();

                long start = Environment.TickCount64;
                foreach (int index in points) {
                    if (visited.Contains(index)) {
                        continue;
                    }
                    Coordinate p = population.getCoordinate(index);
                    var location = SimplePointInAreaLocator.Locate(p, geom);
                    if (location == Location.Interior) {
                        // if (p.getPoint().within(geom)) {
                        Access access;
                        if (accessibilities[index] == null) {
                            access = new Access();
                            accessibilities[index] = access;
                        }
                        else {
                            access = accessibilities[index];
                        }
                        accessibilities[index].access += (float)factor;
                        if (access.access > max_value) {
                            max_value = access.access;
                        }
                    }
                }
                long end = Environment.TickCount64;
                Console.WriteLine("time: " + (end - start));
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

        public async Task calcAccessibility2(double[][] facilities, List<double> ranges, List<double> factors)
        {
            var accessibilities = new Access[this.population.pointCount()];

            ISourceBlock<IsoRaster?> collection = provider.requestIsoRasterStream(facilities, ranges[ranges.Count - 1]);

            float max_value = 0;
            for (int j = 0; j < facilities.Length; j++) {
                var raster = await collection.ReceiveAsync();
                if (raster == null) {
                    continue;
                }
                double[][] extend = raster.getEnvelope();
                Envelope env = new Envelope(extend[0][0], extend[3][0], extend[2][1], extend[1][1]);
                List<int> points = population.getPointsInEnvelop(env);

                long start = Environment.TickCount64;
                foreach (int index in points) {
                    Coordinate p = population.getCoordinate(index, "EPSG:25832");
                    var range_dict = raster.getValueAtCoordinate(p);
                    if (range_dict == null) {
                        continue;
                    }
                    bool found = range_dict.TryGetValue(0, out int range);
                    if (found) {
                        Access access;
                        if (accessibilities[index] == null) {
                            access = new Access();
                            accessibilities[index] = access;
                        }
                        else {
                            access = accessibilities[index];
                        }
                        for (int i = 0; i < ranges.Count; i++) {
                            if (range <= ranges[i]) {
                                access.access += (float)factors[i];
                                if (access.access > max_value) {
                                    max_value = access.access;
                                }
                                break;
                            }
                        }
                    }
                }
                long end = Environment.TickCount64;
                Console.WriteLine("time: " + (end - start));
            }

            for (int index = 0; index < accessibilities.Length; index++) {
                Access access = accessibilities[index];
                if (access.access == 0) {
                    access.access = -9999;
                    access.weighted_access = -9999;
                }
                else {
                    access.access = access.access * 100 / max_value;
                    access.weighted_access = access.access * this.population.getPopulation(index) / max_population;
                }
            }
            this.accessibility = accessibilities;
        }
    }
}