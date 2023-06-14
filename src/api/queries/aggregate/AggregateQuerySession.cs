using System;
using System.Collections.Generic;
using DVAN.Accessibility;
using DVAN.Population;
using DVAN.Routing;
using NetTopologySuite.Geometries;

namespace DVAN.API
{
    class AggregateQuerySession
    {
        public Guid id { get; set; }

        public IPopulationView population_view { get; set; }

        public ICatchment catchment { get; set; }
    }
}