using System;

namespace DVAN.Accessibility
{
    public class LinearDecay : IDistanceDecay
    {
        float max_distance;

        public LinearDecay(float max_distance)
        {
            this.max_distance = max_distance;
        }

        public float getDistanceWeight(float distance)
        {
            if (distance >= max_distance) {
                return 0;
            }
            else {
                return 1 - (distance / max_distance);
            }
        }
    }
}