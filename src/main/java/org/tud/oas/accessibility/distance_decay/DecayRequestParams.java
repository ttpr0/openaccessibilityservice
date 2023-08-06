package org.tud.oas.accessibility.distance_decay;

import com.fasterxml.jackson.annotation.JsonProperty;

/// <summary>
/// Parameters for the distance decay.
/// Depending on the parameters different distance decay function will be used.
/// </summary>
public class DecayRequestParams {
    /// <summary>
    /// Type of distance decay (e.g. hybrid, exponential, linear, ...).
    /// </summary>
    /// <example>hybrid</example>
    @JsonProperty("decay_type")
    public String decay_type;

    /// <summary>
    /// Range at which decay function will be dropped to zero.
    /// Depending on the routing metric used in seconds or meters.
    /// </summary>
    /// <example>900</example>
    @JsonProperty("max_range")
    public Float max_range;

    /// <summary>
    /// Only for hybrid decay.
    /// Upper bounds of decay steps.
    /// Depending on the routing metric used in seconds or meters.
    /// </summary>
    /// <example>[150, 300, 450, 600, 750, 900]</example>
    @JsonProperty("ranges")
    public float[] ranges;

    /// <summary>
    /// Only for hybrid decay.
    /// Factors for decay steps.
    /// </summary>
    /// <example>[1.0, 0.8, 0.5, 0.4, 0.3, 0.1]</example>
    @JsonProperty("range_factors")
    public float[] range_factors;
}
