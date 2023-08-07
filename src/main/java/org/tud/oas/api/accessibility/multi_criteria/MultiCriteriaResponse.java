package org.tud.oas.api.accessibility.multi_criteria;

import java.util.Map;

/// <summary>
/// Multi-criteria response.
/// </summary>
public class MultiCriteriaResponse {
    /// <summary>
    /// Multi-criteria values.
    /// </summary>
    /// <example>[{"multiCriteria": 11.3, "multiCriteria_weighted": 14.3}]</example>
    public Map<String, float[]> access;

    public MultiCriteriaResponse(Map<String, float[]> access) {
        this.access = access;
    }
}
