using System;
using System.Collections.Generic;

namespace DVAN.API
{
    /// <summary>
    /// Grid response.
    /// Can be used to visualize grid as map-layer.
    /// </summary>
    public class GridResponse
    {
        public Guid? id { get; set; }

        /// <summary>
        /// Grid coordinate reference system.
        /// </summary>
        /// <example>EPSG:25832</example>
        public string crs { get; set; }
        /// <summary>
        /// Grid extend ([minx, miny, maxx, maxy]).
        /// </summary>
        /// <example>[9.11, 50.98, 10.23, 52.09]</example>
        public float[] extend { get; set; }
        /// <summary>
        /// Grid size ([rows, cols]).
        /// </summary>
        /// <example>[20, 20]</example>
        public int[] size { get; set; }
        /// <summary>
        /// List of grid features.
        /// </summary>
        public List<GridFeature> features { get; set; }

        public GridResponse(List<GridFeature> features, string crs, float[] extend, int[] size)
        {
            this.crs = crs;
            this.extend = extend;
            this.size = size;
            this.features = features;
        }
    }

    public class GridFeature
    {
        /// <summary>
        /// Feature x coordinate (in specified crs).
        /// </summary>
        /// <example>500233</example>
        public float x { get; set; }
        /// <summary>
        /// Feature y coordinate (in specified crs).
        /// </summary>
        /// <example>534395</example>
        public float y { get; set; }
        /// <summary>
        /// Feature value at "x" and "y" coordinate.
        /// Contains object with keys and values (e.g. {first: 10, second: 23, multi: 100}).
        /// </summary>
        /// <example>{}</example>
        public object value { get; set; }

        public GridFeature(float x, float y, object value)
        {
            this.x = x;
            this.y = y;
            this.value = value;
        }
    }
}
