using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using DVAN.API;
using DVAN.Population;
using NetTopologySuite.Geometries;

namespace DVAN.Accessibility
{
    public class MultiCriteraAccessibility {
        private PopulationView population;
        private GravityAccessibility gravity;

        private float max_population;
        private float max_value;
        private float max_weighted_value;

        private Dictionary<int, Dictionary<string, float>> accessibilities;

        public MultiCriteraAccessibility(PopulationView population, GravityAccessibility gravity) 
        {
            this.population = population;
            this.gravity = gravity;

            float max_pop = 100;
            this.max_population = max_pop;

            this.accessibilities = new Dictionary<int, Dictionary<string, float>>(10000);
        }

        public async Task addAccessibility(String name, Double[][] facilities, List<Double> ranges, List<Double> factors, double weight)
        {
            await gravity.calcAccessibility(facilities, ranges, factors);
            Dictionary<int, Access> accessibility = gravity.getAccessibility();

            Access defaultAccess = new Access();
            defaultAccess.access = -9999;
            defaultAccess.weighted_access = -9999;
            foreach (int index in accessibility.Keys) {
                Access access = accessibility.ContainsKey(index) ? accessibility[index] : defaultAccess;
                Dictionary<string, float> multi_access;
                if (!this.accessibilities.ContainsKey(index)) {
                    this.accessibilities[index] = new Dictionary<string, float>();
                    multi_access = this.accessibilities[index];
                    multi_access["multiCritera"]  = 0.0f;
                    multi_access["multiCritera_weighted"] = 0.0f;
                } else {
                    multi_access = this.accessibilities[index];
                }
                multi_access[name] = access.access;
                multi_access[name + "_weighted"] = access.weighted_access;
                if (access.access == -9999) {
                    continue;
                }
                float temp = multi_access["multiCritera"];
                float weighted_temp = multi_access["multiCritera_weighted"];
                float new_value = temp + access.access;
                float new_weighted_value = weighted_temp + access.access * population.getPopulationCount(index) / max_population;
                multi_access["multiCritera"] = new_value;
                multi_access["multiCritera_weighted"] = new_weighted_value;
                if (new_value > max_value) {
                    max_value = new_value;
                }
                if (new_weighted_value > max_weighted_value) {
                    max_weighted_value = new_weighted_value;
                }
            }
        }

        public void calcAccessibility() 
        {
            foreach (int index in this.accessibilities.Keys) {
                Dictionary<string, float> multi_access = this.accessibilities[index];
                float temp = multi_access["multiCritera"];
                float weighted_temp = multi_access["multiCritera_weighted"];
                if (temp == 0.0) {
                    multi_access["multiCritera"] = -9999.0f;
                    multi_access["multiCritera_weighted"] = -9999.0f;
                }
                else {
                    multi_access["multiCritera"] = temp * 100 / max_value;
                    multi_access["multiCritera_weighted"] = weighted_temp * 100 / max_weighted_value;
                }
            }
        }

        public GridResponse buildResponse() 
        {
            List<GridFeature> features = new List<GridFeature>();
            float minx = 1000000000;
            float maxx = -1;
            float miny = 1000000000;
            float maxy = -1;
            List<int> indices = population.getAllPoints();
            foreach (int index in indices) {
                Coordinate p = population.getCoordinate(index, "EPSG:25832");
                Dictionary<string, float> values;
                if (this.accessibilities.ContainsKey(index)) {
                    values = this.accessibilities[index];
                } else {
                    values = new Dictionary<string, float>();
                    values["multiCritera"] = -9999.0f;
                    values["multiCritera_weighted"] = -9999.0f;
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
                features.Add(new GridFeature((float)p.X, (float)p.Y, values));
            }
            float[] extend = {minx-50, miny-50, maxx+50, maxy+50};

            float dx = extend[2] - extend[0];
            float dy = extend[3] - extend[1];
            int[] size = {(int)(dx/100), (int)(dy/100)};

            String crs = "EPSG:25832";

            return new GridResponse(features, crs, extend, size);
        }
    }
}
