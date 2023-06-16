using System;
using System.Linq;
using System.Collections.Generic;

namespace DVAN.Routing
{
    public class Catchment : ICatchment
    {
        public List<int>[] sources { get; set; }

        public Catchment(List<int>[] sources)
        {
            this.sources = sources;
        }

        public IEnumerable<int> getNeighbours(int destination)
        {
            var agg = this.sources[destination];
            if (agg == null) {
                return Enumerable.Empty<int>();
            }
            return agg;
        }
    }
}