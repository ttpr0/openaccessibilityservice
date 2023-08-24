package org.tud.oas.api.queries.aggregate;

import java.util.UUID;

import org.tud.oas.requests.DemandRequestParams;
import org.tud.oas.requests.RoutingRequestParams;
import org.tud.oas.requests.SupplyRequestParams;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AggregateQueryRequest", description = """
                Aggregate Query Request.
                """)
public class AggregateQueryRequest {
        @Schema(name = "session_id", description = """
                        Session id. Used to reuse precomputed aggregation.
                        """, example = "smlf-dmxm-xdsd-yxdx")
        public UUID session_id;

        public DemandRequestParams demand;

        public RoutingRequestParams routing;

        public SupplyRequestParams supply;

        @Schema(name = "range", description = """
                        Catchment range in seconds.
                        """, example = "900")
        public Double range;

        @Schema(name = "compute_type", description = """
                        Calculation mode (one of "mean", "median", "min", "max").
                        """, example = "mean")
        public String compute_type;
}