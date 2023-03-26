using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;

namespace DVAN.API
{
    public class NearestQueryRequest
    {
        public double[][] facility_locations { get; set; }

        public int facility_count { get; set; }

        public string range_type { get; set; }

        public double? range_max { get; set; }

        public List<double>? ranges { get; set; }

        public double[]? envelop { get; set; }
    }

    public class NearestQueryResultRequest
    {
        public Guid id { get; set; }
    }

    public class NearestQueryComputedRequest
    {
        public Guid id { get; set; }

        public string compute_type { get; set; }

        public List<int> range_indizes { get; set; }
    }

    public class NearestQueryStatisticsRequest
    {
        public Guid id { get; set; }

        public double[]? envelop { get; set; }
    }
}