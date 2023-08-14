package org.tud.oas.api.fca;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FCAResponse", description = """
        FCA response.
        """)
public class FCAResponse {
    @Schema(name = "access", description = """
            Accessibility values.
            """, example = "[72.34, 29.98, 99.21]")
    public float[] access;

    public FCAResponse(float[] access) {
        this.access = access;
    }
}
