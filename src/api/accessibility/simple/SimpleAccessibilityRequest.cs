using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;
using DVAN.Population;

namespace DVAN.API
{
    public class SimpleAccessibilityRequest
    {
        public List<double> ranges { get; set; }

        public double[][] facility_locations { get; set; }

        public PopulationRequestParams population { get; set; }
    }
}