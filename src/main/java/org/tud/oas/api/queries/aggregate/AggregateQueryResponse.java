package org.tud.oas.api.queries.aggregate;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AggregateQueryResponse", description = """
        Aggregate Query Response.
        """)
public class AggregateQueryResponse {
    @Schema(name = "result", description = """
            Aggregation results.
            """, example = "[72.34, 29.98, 99.21]")
    public float[] result;

    @Schema(name = "session_id", description = """
            Session id. Can be used in subsequent aggregate-query requests.
            """, example = "smlf-dmxm-xdsd-yxdx")
    public UUID session_id;

    public AggregateQueryResponse(float[] result, UUID session_id) {
        this.result = result;
        this.session_id = session_id;
    }
}
