package org.tud.oas.api.core.matrix;

import io.swagger.v3.oas.annotations.media.Schema;

class NearestItem {
    public int id;
    public float range;

    public NearestItem(int id, float range) {
        this.id = id;
        this.range = range;
    }
}

@Schema(name = "MatrixResponse", description = """
        Matrix response.
        """)
public class MatrixResponse {
    @Schema(name = "matrix", description = """
            Accessibility values.
            """, example = "[[100.2, 72.4, 25.2], [13.5, 132.4, 52.7]]")
    public float[][] matrix;

    public MatrixResponse(float[][] matrix) {
        this.matrix = matrix;
    }
}
