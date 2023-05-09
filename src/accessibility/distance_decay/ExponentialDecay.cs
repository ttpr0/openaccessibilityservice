using System;

namespace DVAN.Accessibility
{
    public class ExponentialDecay : IDistanceDecay
    {
        float max_distance;

        float impedance;

        public ExponentialDecay(float max_distance)
        {
            this.max_distance = max_distance;
            this.impedance = (float)(-Math.Log(0.01) / max_distance);
        }

        public float getDistanceWeight(float distance)
        {
            if (distance >= max_distance) {
                return 0;
            }
            else {
                return (float)Math.Exp(-distance * impedance);
            }
        }
    }
}