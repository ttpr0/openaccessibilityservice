

namespace DVAN.API
{
    public class GridFeature 
    {
        public float x { get; set; }
        public float y { get; set; }
        public object value { get; set; }
        
        public GridFeature(float x, float y, object value) {
            this.x = x;
            this.y = y;
            this.value = value;
        }
    }
}