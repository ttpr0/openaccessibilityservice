using System;

namespace DVAN.Accessibility
{
    public class PolynomDecay : IDistanceDecay
    {
        float max_distance;

        float[] factors;

        public PolynomDecay(float max_distance, float[] factors)
        {
            this.max_distance = max_distance;
            this.factors = factors;
        }

        public float getDistanceWeight(float distance)
        {
            if (distance >= max_distance) {
                return 0;
            }
            int degree = factors.Length - 1;
            double weight = factors[degree];
            for (int i = 0; i < degree; i++) {
                weight += Math.Pow(distance, degree - i) * factors[i];
            }
            return (float)weight;
        }
    }
}