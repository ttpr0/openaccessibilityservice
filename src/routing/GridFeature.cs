using System;

namespace DVAN.Routing
{
    public class GridFeature
    {
        public float x { get; set; }
        public float y { get; set; }
        public GridValue value { get; set; }

        public GridFeature() { }

        public GridFeature(float x, float y, GridValue value)
        {
            this.x = x;
            this.y = y;
            this.value = value;
        }
    }
}