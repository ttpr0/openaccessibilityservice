package org.tud.oas.api.accessibility.gravity;

/// <summary>
/// Gravity Response.
/// </summary>
public class GravityResponse {
    /// <summary>
    /// Gravity values.
    /// </summary>
    /// <example>[72.34, 29.98, 99.21]</example>
    public float[] access;

    public GravityResponse(float[] access) {
        this.access = access;
    }
}
