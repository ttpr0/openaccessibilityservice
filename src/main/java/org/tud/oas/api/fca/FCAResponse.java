package org.tud.oas.api.fca;

/// <summary>
/// FCA Response.
/// </summary>
public class FCAResponse {
    /// <summary>
    /// FCA Response.
    /// </summary>
    /// <example>[72.34, 29.98, 99.21]</example>
    public float[] access;

    public FCAResponse(float[] access) {
        this.access = access;
    }
}
