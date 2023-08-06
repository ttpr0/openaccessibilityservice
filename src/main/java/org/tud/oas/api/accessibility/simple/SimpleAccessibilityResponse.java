package org.tud.oas.api.accessibility.simple;

/// <summary>
/// Multi-criteria response.
/// </summary>
public class SimpleAccessibilityResponse {
    /// <summary>
    /// Simple-accessibility values.
    /// </summary>
    public SimpleValue[] access;

    public SimpleAccessibilityResponse(SimpleValue[] access) {
        this.access = access;
    }
}

/// <summary>
/// Simple value.
/// </summary>
class SimpleValue {
    /// <summary>
    /// Range to closest facility.
    /// </summary>
    /// <example>123</example>
    public int first;

    /// <summary>
    /// Range to second closest facility.
    /// </summary>
    /// <example>235</example>
    public int second;

    /// <summary>
    /// Range to third closest facility.
    /// </summary>
    /// <example>412</example>
    public int third;

    public SimpleValue(int first, int second, int third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}
