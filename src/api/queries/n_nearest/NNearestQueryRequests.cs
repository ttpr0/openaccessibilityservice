using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;
using DVAN.Population;
using DVAN.Routing;

namespace DVAN.API
{
    /// <summary>
    /// N-Nearest Query Request.
    /// </summary>
    public class NNearestQueryRequest
    {
        /// <summary>
        /// Session id. Used to reuse precomputed aggregation
        /// </summary>
        /// <example>smlf-dmxm-xdsd-yxdx</example>
        public Guid? session_id { get; set; }

        public PopulationRequestParams? population { get; set; }

        public RoutingRequestParams? routing { get; set; }

        /// <summary>
        /// Facility Locations in geographic coordinates.
        /// </summary>
        /// <example>[[9.11, 51.23], [9.35, 50.98], [10.02, 52.10]]</example>
        public double[][]? facility_locations { get; set; }

        /// <summary>
        /// Facility values that will be aggregated.
        /// </summary>
        /// <example>[100.1, 23.8, 107.8]</example>
        public double[]? facility_values { get; set; }

        /// <summary>
        /// Range-type to be used (One of "continuus", "discrete").
        /// </summary>
        /// <example>discrete</example>
        public string? range_type { get; set; }

        /// <summary>
        /// Maximum range (for continuus range_type) in seconds.
        /// </summary>
        /// <example>900</example>
        public double? range_max { get; set; }

        /// <summary>
        /// Ranges (in sec) to be used by calculation.
        /// </summary>
        /// <example>[180, 360, 540, 720, 900]</example>
        public List<double>? ranges { get; set; }

        /// <summary>
        /// Calculation mode (one of "mean", "median", "min", "max").
        /// </summary>
        /// <example>mean</example>
        public string compute_type { get; set; }

        /// <summary>
        /// Number (n) of closest facilities to be used.
        /// </summary>
        /// <example>3</example>
        public int facility_count { get; set; }
    }
}