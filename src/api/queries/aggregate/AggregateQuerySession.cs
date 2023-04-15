using System;
using System.Collections.Generic;
using DVAN.Accessibility;
using DVAN.Population;
using NetTopologySuite.Geometries;

namespace DVAN.API
{
    class AggregateQuerySession
    {
        public Guid id { get; set; }

        public IPopulationView population_view { get; set; }

        public Dictionary<int, List<int>> accessibilities { get; set; }
    }
}