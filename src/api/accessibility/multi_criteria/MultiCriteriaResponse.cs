using System;
using System.Collections.Generic;

namespace DVAN.API
{
    /// <summary>
    /// Multi-criteria response.
    /// </summary>
    public class MultiCriteriaResponse
    {
        /// <summary>
        /// Multi-criteria values.
        /// </summary>
        /// <example>[{"multiCriteria": 11.3, "multiCriteria_weighted": 14.3}]</example>
        public Dictionary<string, float>[] access { get; set; }
    }
}
