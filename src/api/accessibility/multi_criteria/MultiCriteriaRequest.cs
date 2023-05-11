using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;
using DVAN.Population;
using DVAN.Routing;

namespace DVAN.API
{
    /// <summary>
    /// Multi-criteria Request.
    /// </summary>
    public class MultiCriteriaRequest
    {
        /// <summary>
        /// infrastructure parameters.
        /// </summary>
        public Dictionary<string, InfrastructureParams> infrastructures { get; set; }

        public PopulationRequestParams population { get; set; }

        public RoutingRequestParams? routing { get; set; }
    }

    /// <summary>
    /// Infrastructure parameter.
    /// </summary>
    public class InfrastructureParams
    {
        /// <summary>
        /// Weight of infrastructure in multi-criteria..
        /// </summary>
        /// <example>0.8</example>
        public double infrastructure_weight { get; set; }

        /// <summary>
        /// Ranges (in sec) to be used by calculation.
        /// </summary>
        /// <example>[180, 360, 540, 720, 900]</example>
        public List<double> ranges { get; set; }

        /// <summary>
        /// Distance-Decay for every catchment (ranges).
        /// </summary>
        /// <example>[1.0, 0.7, 0.5, 0.3, 0.1]</example>
        public List<double> range_factors { get; set; }

        /// <summary>
        /// Facility Locations in geographic coordinates.
        /// </summary>
        /// <example>[[9.11, 51.23], [9.35, 50.98], [10.02, 52.10]]</example>
        public double[][] facility_locations { get; set; }
    }
}