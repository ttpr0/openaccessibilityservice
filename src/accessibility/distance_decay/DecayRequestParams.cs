using System;

namespace DVAN.Accessibility
{
    /// <summary>
    /// Parameters for the distance decay.
    /// Depending on the parameters different distance decay function will be used.
    /// </summary>
    public class DecayRequestParams
    {
        /// <summary>
        /// Type of distance decay (e.g. hybrid, exponential, linear, ...).
        /// </summary>
        /// <example>hybrid</example>
        public string? decay_type { get; set; }

        /// <summary>
        /// Range at which decay function will be dropped to zero.
        /// Depending on the routing metric used in seconds or meters.
        /// </summary>
        /// <example>900</example>
        public float? max_range { get; set; }

        /// <summary>
        /// Only for hybrid decay.
        /// Upper bounds of decay steps.
        /// Depending on the routing metric used in seconds or meters.
        /// </summary>
        /// <example>[150, 300, 450, 600, 750, 900]</example>
        public float[]? ranges { get; set; }

        /// <summary>
        /// Only for hybrid decay.
        /// Factors for decay steps.
        /// </summary>
        /// <example>[1.0, 0.8, 0.5, 0.4, 0.3, 0.1]</example>
        public float[]? range_factors { get; set; }
    }
}
