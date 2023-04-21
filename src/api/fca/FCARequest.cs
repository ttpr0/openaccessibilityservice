using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;
using DVAN.Population;

namespace DVAN.API
{
    public class FCARequest
    {
        public List<double>? ranges { get; set; }

        public List<double>? range_factors { get; set; }

        public PopulationRequestParams population { get; set; }

        public double[][]? facility_locations { get; set; }

        public string? mode { get; set; }
    }
}
