using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;
using DVAN.Population;

namespace DVAN.API
{
    /// <summary>
    /// Aggregate Query Request.
    /// </summary>
    public class AggregateQueryRequest
    {
        /// <summary>
        /// Session id. Used to reuse precomputed aggregation
        /// </summary>
        /// <example>smlf-dmxm-xdsd-yxdx</example>
        public Guid? session_id { get; set; }

        public PopulationRequestParams? population { get; set; }

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
        /// Catchment range in seconds.
        /// </summary>
        /// <example>900</example>
        public double? range { get; set; }

        /// <summary>
        /// Calculation mode (one of "mean", "median", "min", "max").
        /// </summary>
        /// <example>mean</example>
        public string compute_type { get; set; }
    }
}