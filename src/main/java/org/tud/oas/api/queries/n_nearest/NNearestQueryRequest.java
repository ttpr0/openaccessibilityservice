package org.tud.oas.api.queries.n_nearest;

import java.util.List;
import java.util.UUID;

import org.tud.oas.requests.DemandRequestParams;
import org.tud.oas.requests.RoutingRequestParams;
import org.tud.oas.requests.SupplyRequestParams;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NNearestQueryRequest", description = """
                N-Nearest Query Request.
                """)
public class NNearestQueryRequest {
        @Schema(name = "session_id", description = """
                        Session id. Used to reuse precomputed aggregation.
                        """, example = "smlf-dmxm-xdsd-yxdx")
        public UUID session_id;

        public DemandRequestParams demand;

        public RoutingRequestParams routing;

        public SupplyRequestParams supply;

        @Schema(name = "range_type", description = """
                        Range-type to be used (One of "continuus", "discrete").
                        """, example = "discrete")
        public String range_type;

        @Schema(name = "range_max", description = """
                        Maximum range (for continuus range_type) in seconds.
                        """, example = "900")
        public Double range_max;

        @Schema(name = "ranges", description = """
                        Ranges (in sec) to be used by calculation.
                        """, example = "[180, 360, 540, 720, 900]")
        public List<Double> ranges;

        @Schema(name = "compute_type", description = """
                        Calculation mode (one of "mean", "median", "min", "max").
                        """, example = "mean")
        public String compute_type;

        @Schema(name = "facility_count", description = """
                        Number (n) of closest facilities to be used.
                        """, example = "3")
        public int facility_count;
}