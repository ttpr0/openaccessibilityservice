package org.tud.oas.api.queries.n_nearest;

import java.util.UUID;

/// <summary>
/// FCA Response.
/// </summary>
public class NNearestQueryResponse {
    /// <summary>
    /// FCA Response.
    /// </summary>
    /// <example>[72.34, 29.98, 99.21]</example>
    public float[] result;

    /// <summary>
    /// Session id. Can be used in subsequent aggregate-query requests.
    /// </summary>
    /// <example>smlf-dmxm-xdsd-yxdx</example>
    public UUID session_id;

    public NNearestQueryResponse(float[] result, UUID session_id) {
        this.result = result;
        this.session_id = session_id;
    }
}
