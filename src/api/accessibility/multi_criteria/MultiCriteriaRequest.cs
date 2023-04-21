using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;
using DVAN.Population;

namespace DVAN.API
{
    public class MultiCriteriaRequest
    {
        public Dictionary<string, InfrastructureParams> infrastructures { get; set; }

        public PopulationRequestParams population { get; set; }
    }

    public class InfrastructureParams
    {
        public double infrastructure_weight { get; set; }

        public List<double> ranges { get; set; }

        public List<double> range_factors { get; set; }

        public double[][] facility_locations { get; set; }
    }
}