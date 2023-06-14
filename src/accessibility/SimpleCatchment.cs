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
        private List<int>[]? accessibilities;

        public SimpleCatchment(IPopulationView population, IRoutingProvider provider)
        {
            this.population = population;
            this.provider = provider;
        }

        public List<int>[]? getAccessibilities()
        {
            return this.accessibilities;
        }

        public async Task calcAccessibility(double[][] facilities, double catchment_range)
        {
            var catchment = await this.provider.requestCatchment(this.population, facilities, catchment_range, "isochrones");
            if (catchment == null) {
                return;
            }
            this.accessibilities = ((Catchment)catchment).sources;
        }
    }
}