using System;
using System.Collections.Generic;
using DVAN.Accessibility;
using DVAN.Population;
using NetTopologySuite.Geometries;

namespace DVAN.API
{
    class NNearestQuerySession
    {
        public Guid id { get; set; }

        public IPopulationView population_view { get; set; }

        public List<RangeRef>[] accessibilities { get; set; }
    }
}