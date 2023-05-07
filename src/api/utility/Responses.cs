using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;
using DVAN.Population;

namespace DVAN.API
{
    /// <summary>
    /// Population store response.
    /// </summary>
    public class PopulationStoreResponse
    {
        /// <summary>
        /// Population view id.
        /// </summary>
        /// <example>sfjf-djfd-omsf-jjfd</example>
        public Guid id { get; set; }
    }

    /// <summary>
    /// Population get response.
    /// </summary>
    public class PopulationGetResponse
    {
        /// <summary>
        /// Locations of population points.
        /// </summary>
        /// <example>[[9.11, 51.23], [9.35, 50.98], [10.02, 52.10]]</example>
        public List<double[]> locations { get; set; }

        /// <summary>
        /// Weights of population points (typically number of people living there).
        /// </summary>
        /// <example>[72, 29, 99]</example>
        public List<double> weights { get; set; }
    }
}
