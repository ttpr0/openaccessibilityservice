using System;

namespace DVAN.Accessibility
{
    public static class DistanceDecay
    {
        public static IDistanceDecay? getDistanceDecay(DecayRequestParams param)
        {
            switch (param.decay_type) {
                case "hybrid":
                    if (param.ranges == null || param.range_factors == null) {
                        return null;
                    }
                    if (param.ranges.Length == 0 || param.range_factors.Length != param.ranges.Length) {
                        return null;
                    }
                    return new HybridDecay(param.ranges, param.range_factors);
                case "binary":
                    if (param.max_range == null) {
                        return null;
                    }
                    if (param.max_range.Value <= 0) {
                        return null;
                    }
                    return new BinaryDecay(param.max_range.Value);
                case "linear":
                    if (param.max_range == null) {
                        return null;
                    }
                    if (param.max_range.Value <= 0) {
                        return null;
                    }
                    return new LinearDecay(param.max_range.Value);
                case "exponential":
                    if (param.max_range == null) {
                        return null;
                    }
                    if (param.max_range.Value <= 0) {
                        return null;
                    }
                    return new ExponentialDecay(param.max_range.Value);
                case "gaussian":
                    if (param.max_range == null) {
                        return null;
                    }
                    if (param.max_range.Value <= 0) {
                        return null;
                    }
                    return new GaussianDecay(param.max_range.Value);
                case "inverse-power":
                    if (param.max_range == null) {
                        return null;
                    }
                    if (param.max_range.Value <= 0) {
                        return null;
                    }
                    return new InversePowerDecay(param.max_range.Value);
                case "kernel-density":
                    if (param.max_range == null) {
                        return null;
                    }
                    if (param.max_range.Value <= 0) {
                        return null;
                    }
                    return new KernelDensityDecay(param.max_range.Value);
                default:
                    return null;
            }
        }
    }
}