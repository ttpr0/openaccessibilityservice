using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;
using DVAN.Population;

namespace DVAN.API
{
    /// <summary>
    /// Request to store population view (from given parameters).
    /// </summary>
    public class PopulationStoreRequest
    {
        public PopulationRequestParams population { get; set; }
    }

    /// <summary>
    /// Request to get data from stored or internal population view.
    /// </summary>
    public class PopulationGetRequest
    {
        /// <summary>
        /// Population view id.
        /// </summary>
        /// <example>sfjf-djfd-omsf-jjfd</example>
        public Guid? population_id { get; set; }

        public PopulationRequestParams? population { get; set; }
    }
}
