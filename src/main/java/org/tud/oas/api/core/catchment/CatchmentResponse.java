package org.tud.oas.api.core.catchment;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

class NearestItem {
    public int id;
    public float range;

    public NearestItem(int id, float range) {
        this.id = id;
        this.range = range;
    }
}

@Schema(name = "CatchmentResponse", description = """
        Catchment response.
        """)
public class CatchmentResponse {
    @Schema(name = "catchment", description = """
            Accessibility values.
            """, example = "[[3, 1], [4, 2]]")
    public List<Integer>[] catchment;

    public CatchmentResponse(List<Integer>[] catchment) {
        this.catchment = catchment;
    }
}
