using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;
using DVAN.Population;

namespace DVAN.API
{
    public class NNearestQueryRequest
    {
        public Guid? session_id { get; set; }

        public PopulationRequestParams? population { get; set; }

        public double[][]? facility_locations { get; set; }

        public double[]? facility_values { get; set; }

        public string? range_type { get; set; }

        public double? range_max { get; set; }

        public List<double>? ranges { get; set; }

        public string compute_type { get; set; }

        public int facility_count { get; set; }
    }
}