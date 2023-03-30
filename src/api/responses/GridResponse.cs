using System;
using System.Collections.Generic;

namespace DVAN.API
{
    public class GridResponse
    {
        public Guid? id { get; set; }
        public String crs { get; set; }
        public float[] extend { get; set; }
        public int[] size { get; set; }
        public List<GridFeature> features { get; set; }

        public GridResponse(List<GridFeature> features, String crs, float[] extend, int[] size)
        {
            this.crs = crs;
            this.extend = extend;
            this.size = size;
            this.features = features;
        }
    }
}
