using System;
using System.Collections.Generic;


namespace DVAN.API
{
    public class FCARequest 
    {
        public List<Double>? ranges { get; set; }

        public List<Double>? range_factors { get; set; }

        public Double[][]? facility_locations { get; set; }
    }
}
