package org.tud.oas.api.accessibility.gravity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "GravityResponse", description = """
        Gravity response.
        """)
public class GravityResponse {
    @Schema(name = "access", description = """
            Accessibility values.
            """, example = "[72.34, 29.98, 99.21]")
    public float[] access;

    public GravityResponse(float[] access) {
        this.access = access;
    }
}
