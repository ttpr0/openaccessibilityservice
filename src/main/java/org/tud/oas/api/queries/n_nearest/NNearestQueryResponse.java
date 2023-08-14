package org.tud.oas.api.queries.n_nearest;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NNearestQueryResponse", description = """
        N-Nearest Query Response.
        """)
public class NNearestQueryResponse {
    @Schema(name = "result", description = """
            Query results.
            """, example = "[72.34, 29.98, 99.21]")
    public float[] result;

    @Schema(name = "session_id", description = """
            Session id. Can be used in subsequent aggregate-query requests.
            """, example = "smlf-dmxm-xdsd-yxdx")
    public UUID session_id;

    public NNearestQueryResponse(float[] result, UUID session_id) {
        this.result = result;
        this.session_id = session_id;
    }
}
