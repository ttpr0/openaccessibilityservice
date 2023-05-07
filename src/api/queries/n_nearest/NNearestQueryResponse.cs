using System;

namespace DVAN.API
{
    /// <summary>
    /// FCA Response.
    /// </summary>
    public class NNearestQueryResponse
    {
        /// <summary>
        /// FCA Response.
        /// </summary>
        /// <example>[72.34, 29.98, 99.21]</example>
        public float[] result { get; set; }

        /// <summary>
        /// Session id. Can be used in subsequent aggregate-query requests.
        /// </summary>
        /// <example>smlf-dmxm-xdsd-yxdx</example>
        public Guid session_id { get; set; }
    }
}
