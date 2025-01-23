package org.tud.oas.services;

import org.springframework.stereotype.Service;
import org.tud.oas.accessibility.distance_decay.BinaryDecay;
import org.tud.oas.accessibility.distance_decay.ExponentialDecay;
import org.tud.oas.accessibility.distance_decay.GaussianDecay;
import org.tud.oas.accessibility.distance_decay.HybridDecay;
import org.tud.oas.accessibility.distance_decay.IDistanceDecay;
import org.tud.oas.accessibility.distance_decay.InversePowerDecay;
import org.tud.oas.accessibility.distance_decay.KernelDensityDecay;
import org.tud.oas.accessibility.distance_decay.LinearDecay;
import org.tud.oas.accessibility.distance_decay.PolynomDecay;
import org.tud.oas.accessibility.distance_decay.PiecewiseLinearDecay;
import org.tud.oas.requests.DecayRequestParams;

@Service
public class DecayService {
    public IDistanceDecay getDistanceDecay(DecayRequestParams param) {
        switch (param.decay_type) {
            case "hybrid":
                if (param.ranges == null || param.range_factors == null) {
                    return null;
                }
                if (param.ranges.length == 0 || param.range_factors.length != param.ranges.length) {
                    return null;
                }
                return new HybridDecay(param.ranges, param.range_factors);
            case "binary":
                if (param.max_range == null) {
                    return null;
                }
                if (param.max_range <= 0) {
                    return null;
                }
                return new BinaryDecay(param.max_range);
            case "linear":
                if (param.max_range == null) {
                    return null;
                }
                if (param.max_range <= 0) {
                    return null;
                }
                return new LinearDecay(param.max_range);
            case "exponential":
                if (param.max_range == null) {
                    return null;
                }
                if (param.max_range <= 0) {
                    return null;
                }
                return new ExponentialDecay(param.max_range);
            case "gaussian":
                if (param.max_range == null) {
                    return null;
                }
                if (param.max_range <= 0) {
                    return null;
                }
                return new GaussianDecay(param.max_range);
            case "inverse-power":
                if (param.max_range == null) {
                    return null;
                }
                if (param.max_range <= 0) {
                    return null;
                }
                return new InversePowerDecay(param.max_range);
            case "kernel-density":
                if (param.max_range == null) {
                    return null;
                }
                if (param.max_range <= 0) {
                    return null;
                }
                return new KernelDensityDecay(param.max_range);
            case "polynom":
                if (param.max_range == null || param.range_factors == null) {
                    return null;
                }
                if (param.max_range <= 0 || param.range_factors.length == 0) {
                    return null;
                }
                return new PolynomDecay(param.max_range, param.range_factors);
            case "piecewise-linear":
                if (param.ranges == null || param.range_factors == null) {
                    return null;
                }
                if (param.ranges.length == 0 || param.range_factors.length != param.ranges.length) {
                    return null;
                }
                return new PiecewiseLinearDecay(param.ranges, param.range_factors);
            default:
                return null;
        }
    }
}
