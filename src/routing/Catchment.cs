using System;
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
            return this.sources[destination];
        }
    }
}