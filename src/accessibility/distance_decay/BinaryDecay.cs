using System;

namespace DVAN.Accessibility
{
    public class BinaryDecay : IDistanceDecay
    {
        float max_distance;

        float factor = (float)1;

        public BinaryDecay(float max_distance)
        {
            this.max_distance = max_distance;
        }

        public float getDistanceWeight(float distance)
        {
            if (distance >= max_distance) {
                return 0;
            }
            else {
                return factor;
            }
        }
    }
}