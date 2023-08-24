package org.tud.oas.api.accessibility.multi_criteria;

import java.util.List;
import java.util.Map;

import org.tud.oas.requests.DecayRequestParams;
import org.tud.oas.requests.DemandRequestParams;
import org.tud.oas.requests.RoutingRequestParams;
import org.tud.oas.requests.SupplyRequestParams;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MultiCriteriaRequest", description = """
                Multi-criteria Request.
                """)
public class MultiCriteriaRequest {
        @Schema(name = "infrastructures", description = """
                        infrastructure parameters.
                        """)
        public Map<String, InfrastructureParams> infrastructures;

        public DemandRequestParams demand;

        public RoutingRequestParams routing;
}

@Schema(name = "InfrastructureParams", description = """
                Infrastructure parameter.
                """)
class InfrastructureParams {
        @Schema(name = "infrastructure_weight", description = """
                        Weight of infrastructure in multi-criteria..
                        """, example = "0.8")
        public double infrastructure_weight;

        public DecayRequestParams decay;

        public SupplyRequestParams supply;
}
