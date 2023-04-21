using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;
using DVAN.Population;

namespace DVAN.API
{
    public class PopulationStoreRequest
    {
        public PopulationRequestParams population { get; set; }
    }

    public class PopulationGetRequest
    {
        public Guid? population_id { get; set; }

        public PopulationRequestParams? population { get; set; }
    }
}
