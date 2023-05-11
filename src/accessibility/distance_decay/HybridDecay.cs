using System;

namespace DVAN.Accessibility
{
    public class HybridDecay : IDistanceDecay
    {
        float[] distances;

        float[] factors;

        public HybridDecay(float[] distances, float[] factors)
        {
            Array.Sort(distances, factors);
            this.distances = distances;
            this.factors = factors;
        }

        public float getDistanceWeight(float distance)
        {
            for (int i = 0; i < distances.Length; i++) {
                if (distance <= distances[i]) {
                    return factors[i];
                }
            }
            return 0;
        }
    }
}