package org.tud.oas.api.core.nearest;

import io.swagger.v3.oas.annotations.media.Schema;

class NearestItem {
    public int id;
    public float range;

    public NearestItem(int id, float range) {
        this.id = id;
        this.range = range;
    }
}

@Schema(name = "NearestResponse", description = """
        Nearest response.
        """)
public class NearestResponse {
    @Schema(name = "nearest", description = """
            Accessibility values.
            """, example = "[{'id': 3, 'range': 72.34}, {'id': 1, 'range': 22.43}]")
    public NearestItem[] nearest;

    public NearestResponse(NearestItem[] nearest) {
        this.nearest = nearest;
    }
}
