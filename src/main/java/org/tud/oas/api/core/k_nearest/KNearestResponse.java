package org.tud.oas.api.core.k_nearest;

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

@Schema(name = "KNearestResponse", description = """
        KNearest response.
        """)
public class KNearestResponse {
    @Schema(name = "k_nearest", description = """
            Accessibility values.
            """, example = "[[{'id': 3, 'range': 72.34}, {'id': 1, 'range': 22.43}], [{'id': 2, 'range': 22.34}]]")
    public List<NearestItem>[] k_nearest;

    public KNearestResponse(List<NearestItem>[] nearest) {
        this.k_nearest = nearest;
    }
}
