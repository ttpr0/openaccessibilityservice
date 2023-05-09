using System;

namespace DVAN.Accessibility
{
    public class InversePowerDecay : IDistanceDecay
    {
        float max_distance;
        float impedance;

        public InversePowerDecay(float max_distance)
        {
            this.max_distance = max_distance;
            this.impedance = (float)Math.Log(1 / 0.01, max_distance);
        }

        public float getDistanceWeight(float distance)
        {
            if (distance >= max_distance) {
                return 0;
            }
            else {
                return (float)Math.Pow(distance, -impedance);
            }
        }
    }
}