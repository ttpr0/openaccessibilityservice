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

        public AggregateQueryResponse(float[] result) {
                this.result = result;
        }
}
