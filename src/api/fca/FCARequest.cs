using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;
using DVAN.Population;
using DVAN.Accessibility;
using DVAN.Routing;

namespace DVAN.API
{
    /// <summary>
    /// FCA Request.
    /// </summary>
    public class FCARequest
    {
        /// <summary>
        /// Ranges (in sec) to be used by calculation.
        /// </summary>
        /// <example>[180, 360, 540, 720, 900]</example>
        public List<double>? ranges { get; set; }

        public PopulationRequestParams population { get; set; }

        public DecayRequestParams distance_decay { get; set; }

        public RoutingRequestParams? routing { get; set; }

        /// <summary>
        /// Facility Locations in geographic coordinates.
        /// </summary>
        /// <example>[[9.11, 51.23], [9.35, 50.98], [10.02, 52.10]]</example>
        public double[][]? facility_locations { get; set; }

        /// <summary>
        /// Facility capacities.
        /// </summary>
        /// <example>[12.1, 43.5, 21.4]</example>
        public double[]? facility_capacities { get; set; }

        /// <summary>
        /// Calculation mode (one of "isochrones", "isoraster", "matrix").
        /// Defaults to "isochrones".
        /// </summary>
        /// <example>isochrones</example>
        public string? mode { get; set; }
    }
}
