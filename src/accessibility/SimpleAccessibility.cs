using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using DVAN.API;
using DVAN.Routing;
using DVAN.Population;
using NetTopologySuite.Algorithm.Locate;
using NetTopologySuite.Geometries;

namespace DVAN.Accessibility
{
    public class SimpleAccessibility 
    {
        private PopulationContainer population;
        private IRoutingProvider provider;
        private Accessibility accessibility;

        public SimpleAccessibility(PopulationContainer population, IRoutingProvider provider) 
        {
            this.population = population;
            this.provider = provider;
        }

        public async Task calcAccessibility(Double[][] facilities, List<Double> ranges)
        {
            float[] population_weights = new float[population.getPointCount()];
            float[] facility_weights = new float[facilities.Length];
            foreach (PopulationAttributes attr in population.attributes) {
                population_weights[attr.getIndex()] = attr.getPopulationCount();
            }
            PopulationAccessibility[] accessibilities = new PopulationAccessibility[population.getPointCount()];
            for (int i = 0; i < accessibilities.Length; i++) {
                accessibilities[i] = new PopulationAccessibility();
            }

            FacilityCatchment[] catchments = new FacilityCatchment[facilities.Length];
            for (int i = 0; i < catchments.Length; i++) {
                catchments[i] = new FacilityCatchment();
            }
            Double[][] locations = new Double[1][];
            for (int f=0; f<facilities.Length; f++) {
                locations[0][0] = facilities[f][0];
                locations[0][1] = facilities[f][1];
                IsochroneCollection isochrones = (await provider.requestIsochrones(locations, ranges))[0];

                for (int i=0; i< isochrones.getIsochronesCount(); i++) {
                    Isochrone isochrone = isochrones.getIsochrone(i);
                    double range = isochrone.getValue();
                    Geometry iso;
                    Geometry outer = isochrone.getGeometry();
                    if (i==0) {
                        iso = outer;
                    }
                    else {
                        Geometry inner = isochrones.getIsochrone(i-1).getGeometry();
                        iso = outer.Difference(inner);
                    }
                    Envelope env = iso.EnvelopeInternal;
                    List<int> points = population.getPointsInEnvelop(env);
                    int population_count = 0;
                    foreach (int index in points) {
                        Coordinate p = population.getPoint(index);
                        var location = SimplePointInAreaLocator.Locate(p, iso);
                        if (location == Location.Interior) {
                            PopulationAttributes attr = population.getAttributes(index);

                            accessibilities[index].addRange((int)range);
                            population_count += attr.getPopulationCount();
                        }
                    }
                    catchments[f].addRangeRef(range, population_count);
                }
            }

            this.accessibility = new Accessibility(accessibilities, catchments);
        }

        public GridResponse buildResponse()
        {
            List<API.GridFeature> features = new List<API.GridFeature>();
            float minx = 1000000000;
            float maxx = -1;
            float miny = 1000000000;
            float maxy = -1;
            for (int i=0; i< population.getPointCount(); i++) {
                Coordinate p = population.getUTMPoint(i);
                PopulationAccessibility feature = accessibility.accessibilities[i];
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
                feature.ranges.Sort((int a, int b) => {
                    return a - b;
                });
                SimpleValue value = new SimpleValue(-9999, -9999, -9999);
                if (feature.ranges.Count > 0){
                    value.first = feature.ranges[0];
                }
                if (feature.ranges.Count > 1){
                    value.second = feature.ranges[1];
                }
                if (feature.ranges.Count > 2){
                    value.third = feature.ranges[2];
                }
                features.Add(new API.GridFeature((float)p.X, (float)p.Y, value));
            }
            float[] extend = {minx-50, miny-50, maxx+50, maxy+50};

            float dx = extend[2] - extend[0];
            float dy = extend[3] - extend[1];
            int[] size = {(int)(dx/100), (int)(dy/100)};

            String crs = "EPSG:25832";

            return new GridResponse(features, crs, extend, size);
        }
    }

    class RangeRef {
        double range;
        int count;

        public RangeRef(double range, int count) {
            this.range = range;
            this.count = count;
        }
    }

    public class FacilityCatchment {
        List<RangeRef> population_counts;

        public FacilityCatchment() {
            this.population_counts = new List<RangeRef>();
        }

        public void addRangeRef(double range, int count) {
            this.population_counts.Add(new RangeRef(range, count));
        }
    }

    class SimpleValue {
        public int first;
        public int second;
        public int third;

        public SimpleValue(int first, int second, int third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }
}