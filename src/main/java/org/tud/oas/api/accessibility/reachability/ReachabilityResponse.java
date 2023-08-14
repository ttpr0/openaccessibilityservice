package org.tud.oas.api.accessibility.reachability;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ReachabilityResponse", description = """
        Reachability response.
        """)
public class ReachabilityResponse {
    @Schema(name = "access", description = """
            Accessibility values.
            """, example = "[72.34, 29.98, 99.21]")
    public float[] access;

    public ReachabilityResponse(float[] access) {
        this.access = access;
    }
}
