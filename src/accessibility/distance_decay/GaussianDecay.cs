using System;

namespace DVAN.Accessibility
{
    public class GaussianDecay : IDistanceDecay
    {
        float max_distance;

        float impedance;

        public GaussianDecay(float max_distance)
        {
            this.max_distance = max_distance;
            this.impedance = (float)(-Math.Pow(max_distance, 2) / Math.Log(0.01));
        }

        public float getDistanceWeight(float distance)
        {
            if (distance >= max_distance) {
                return 0;
            }
            else {
                return (float)Math.Exp(-Math.Pow(distance, 2) / impedance);
            }
        }
    }
}