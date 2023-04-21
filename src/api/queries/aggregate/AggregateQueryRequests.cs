using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;
using DVAN.Population;

namespace DVAN.API
{
    public class AggregateQueryRequest
    {
        public Guid? session_id { get; set; }

        public PopulationRequestParams? population { get; set; }

        public double[][]? facility_locations { get; set; }

        public double[]? facility_values { get; set; }

        public double? range { get; set; }

        public string compute_type { get; set; }
    }
}