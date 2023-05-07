using System;
using System.Collections.Generic;

namespace DVAN.API
{
    /// <summary>
    /// Multi-criteria response.
    /// </summary>
    public class SimpleAccessibilityResponse
    {
        /// <summary>
        /// Simple-accessibility values.
        /// </summary>
        public SimpleValue[] access { get; set; }
    }

    /// <summary>
    /// Simple value.
    /// </summary>
    public class SimpleValue
    {
        /// <summary>
        /// Range to closest facility.
        /// </summary>
        /// <example>123</example>
        public int first { get; set; }

        /// <summary>
        /// Range to second closest facility.
        /// </summary>
        /// <example>235</example>
        public int second { get; set; }

        /// <summary>
        /// Range to third closest facility.
        /// </summary>
        /// <example>412</example>
        public int third { get; set; }

        public SimpleValue(int first, int second, int third)
        {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }
}
