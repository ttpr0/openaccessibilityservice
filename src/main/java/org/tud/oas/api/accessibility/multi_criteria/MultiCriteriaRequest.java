package org.tud.oas.api.accessibility.multi_criteria;

import java.util.List;
import java.util.Map;

import org.tud.oas.requests.AccessResponseParams;
import org.tud.oas.requests.DecayRequestParams;
import org.tud.oas.requests.DemandRequestParams;
import org.tud.oas.requests.RoutingRequestParams;
import org.tud.oas.requests.SupplyRequestParams;

import com.fasterxml.jackson.annotation.JsonProperty;

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

        @Schema(name = "return_all", description = """
                        If true all individuel results will be returned.
                        Else only multi-criteria result.
                        """)
        public Boolean return_all;

        @Schema(name = "return_weighted", description = """
                        If true all access weighted by demand-count will be computed ("{name}_weighted").
                        """)
        public Boolean return_weighted;

        @JsonProperty("response")
        public AccessResponseParams response_params;
}

@Schema(name = "InfrastructureParams", description = """
                Infrastructure parameter.
                """)
class InfrastructureParams {
        @Schema(name = "infrastructure_weight", description = """
                        Weight of infrastructure in multi-criteria..
                        """, example = "0.8")
        public float infrastructure_weight;

        public DecayRequestParams decay;

        public SupplyRequestParams supply;
}
