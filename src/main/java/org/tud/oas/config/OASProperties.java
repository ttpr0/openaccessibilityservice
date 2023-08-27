package org.tud.oas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.tud.oas.util.YamlPropertySourceFactory;

@Component
@PropertySource(value = "file:oas-config.yml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties
public class OASProperties {
    private RoutingProperties routing;
    private DemandProperties demand;

    public RoutingProperties getRouting() {
        return routing;
    }

    public void setRouting(RoutingProperties routing) {
        this.routing = routing;
    }

    public DemandProperties getDemand() {
        return demand;
    }

    public void setDemand(DemandProperties demand) {
        this.demand = demand;
    }
}
