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
    public class Access {
        public float access;
        public float weighted_access;
    }

    public class GravityAccessibility {
        private PopulationView population;
        private IRoutingProvider provider;

        private float max_population;
        private Dictionary<int, Access> accessibility;

        public GravityAccessibility(PopulationView population, IRoutingProvider provider) 
        {
            this.population = population;
            this.provider = provider;

            this.max_population = 100;
            this.accessibility = new Dictionary<int, Access>();
        }

        public Dictionary<int, Access> getAccessibility() 
        {
            return this.accessibility;
        }

        public async Task calcAccessibility(Double[][] facilities, List<Double> ranges, List<Double> factors)
        {
            HashSet<int> visited = new HashSet<int>(10000);
            Dictionary<int, Access> accessibilities = new Dictionary<int, Access>(10000);

            Dictionary<double, Geometry> polygons = new Dictionary<double, Geometry>(ranges.Count);

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
            for (int j=0; j<facilities.Length; j++) {
                var isochrones = await collection.ReceiveAsync();
                if (isochrones == null) {
                    continue;
                }

                for (int i=0; i< isochrones.getIsochronesCount(); i++) {
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
            for (int i=0; i<ranges.Count; i++) {
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
                        if (!accessibilities.ContainsKey(index)) {
                            access = new Access();
                            accessibilities[index] = access;
                        } else {
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

            foreach (int key in accessibilities.Keys) {
                Access access = accessibilities[key];
                if (access.access == 0) {
                    access.access = -9999;
                    access.weighted_access = -9999;
                }
                else {
                    access.access = access.access * 100 / max_value;
                    access.weighted_access = access.access * this.population.getPopulationCount(key) / max_population;
                }
            }
            this.accessibility = accessibilities;
        }

        public async Task calcAccessibility2(Double[][] facilities, List<Double> ranges, List<Double> factors)
        {
            Dictionary<int, Access> accessibilities = new Dictionary<int, Access>(10000);

            ISourceBlock<IsoRaster?> collection = provider.requestIsoRasterStream(facilities, ranges[ranges.Count-1]);

            float max_value = 0;
            for (int j=0; j<facilities.Length; j++) {
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
                    int range = raster.getValueAtCoordinate(p);
                    if (range != -1) {
                        Access access;
                        if (!accessibilities.ContainsKey(index)) {
                            access = new Access();
                            accessibilities[index] = access;
                        } else {
                            access = accessibilities[index];
                        }
                        for (int i=0; i<ranges.Count; i++) {
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

            foreach (int index in accessibilities.Keys) {
                Access access = accessibilities[index];
                if (access.access == 0) {
                    access.access = -9999;
                    access.weighted_access = -9999;
                }
                else {
                    access.access = access.access * 100 / max_value;
                    access.weighted_access = access.access * this.population.getPopulationCount(index) / max_population;
                }
            }
            this.accessibility = accessibilities;
        }

        public GridResponse buildResponse() {
            List<API.GridFeature> features = new List<API.GridFeature>();
            float minx = 1000000000;
            float maxx = -1;
            float miny = 1000000000;
            float maxy = -1;
            List<int> indices = population.getAllPoints();
            for (int i=0; i< indices.Count; i++) {
                int index = indices[i];
                Coordinate p = population.getCoordinate(index, "EPSG:25832");
                if (this.accessibility.ContainsKey(index)) {
                    Access access = this.accessibility[index];
                    GravityValue value = new GravityValue(access.access, access.weighted_access);
                    features.Add(new API.GridFeature((float)p.X, (float)p.Y, value));
                } else {
                    GravityValue value = new GravityValue(-9999, -9999);
                    features.Add(new API.GridFeature((float)p.X, (float)p.Y, value));
                }
                if (p.X < minx) {
                    minx = (float)p.X;
                }
                if (p.X > maxx) {
                    maxx = (float)p.X;
                }
                if (p.Y < miny) {
                    miny = (float)p.Y;
                }
                if (p.Y > maxy) {
                    maxy = (float)p.Y;
                }
            }
            float[] extend = {minx-50, miny-50, maxx+50, maxy+50};

            float dx = extend[2] - extend[0];
            float dy = extend[3] - extend[1];
            int[] size = {(int)(dx/100), (int)(dy/100)};

            String crs = "EPSG:25832";

            return new GridResponse(features, crs, extend, size);
        }
    }

    class GravityValue {
        public float unweighted { get; set; }
        public float weighted { get; set; }

        public GravityValue(float unweighted, float weighted) {
            this.unweighted = unweighted;
            this.weighted = weighted;
        }
    }
}