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
        private List<RangeRef>[] accessibilities;

        public SimpleAccessibility(IPopulationView population, IRoutingProvider provider)
        {
            this.population = population;
            this.provider = provider;
        }

        public List<RangeRef>[] getAccessibilities()
        {
            return this.accessibilities;
        }

        public async Task calcAccessibility(double[][] facilities, List<double> ranges)
        {
            var accessibilities = new List<RangeRef>[this.population.pointCount()];

            FacilityCatchment[] catchments = new FacilityCatchment[facilities.Length];
            for (int i = 0; i < catchments.Length; i++) {
                catchments[i] = new FacilityCatchment();
            }

            var matrix = await this.provider.requestTDMatrix(this.population, facilities, ranges, "isochrones");
            for (int f = 0; f < facilities.Length; f++) {
                for (int p = 0; p < this.population.pointCount(); p++) {
                    float range = matrix.getRange(f, p);
                    if (range == 9999) {
                        continue;
                    }
                    List<RangeRef> access;
                    if (accessibilities[p] == null) {
                        access = new List<RangeRef>();
                        accessibilities[p] = access;
                    }
                    else {
                        access = accessibilities[p];
                    }
                    accessibilities[p].Add(new RangeRef((int)range, f));
                }
            }

            this.accessibilities = accessibilities;
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