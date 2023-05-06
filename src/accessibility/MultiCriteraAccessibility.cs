using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using DVAN.API;
using DVAN.Population;
using NetTopologySuite.Geometries;

namespace DVAN.Accessibility
{
    public class MultiCriteraAccessibility
    {
        private IPopulationView population;
        private GravityAccessibility gravity;

        private float max_population;
        private float max_value;
        private float max_weighted_value;

        private Dictionary<string, float>[] accessibilities;

        public MultiCriteraAccessibility(IPopulationView population, GravityAccessibility gravity)
        {
            this.population = population;
            this.gravity = gravity;

            float max_pop = 100;
            this.max_population = max_pop;

            this.accessibilities = new Dictionary<string, float>[population.pointCount()];
        }

        public Dictionary<string, float>[] getAccessibilities()
        {
            return this.accessibilities;
        }

        public async Task addAccessibility(string name, double[][] facilities, List<double> ranges, List<double> factors, double weight)
        {
            await gravity.calcAccessibility(facilities, ranges, factors);
            Access[] accessibility = gravity.getAccessibility();

            Access defaultAccess = new Access();
            defaultAccess.access = -9999;
            defaultAccess.weighted_access = -9999;
            for (int index = 0; index < accessibility.Length; index++) {
                Access access = accessibility[index] != null ? accessibility[index] : defaultAccess;
                Dictionary<string, float> multi_access;
                if (this.accessibilities[index] == null) {
                    this.accessibilities[index] = new Dictionary<string, float>();
                    multi_access = this.accessibilities[index];
                    multi_access["multiCritera"] = 0.0f;
                    multi_access["multiCritera_weighted"] = 0.0f;
                }
                else {
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
                float new_weighted_value = weighted_temp + access.access * population.getPopulation(index) / max_population;
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
            for (int index = 0; index < this.accessibilities.Length; index++) {
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
    }
}
