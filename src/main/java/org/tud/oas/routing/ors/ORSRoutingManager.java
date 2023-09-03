package org.tud.oas.routing.ors;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.tud.oas.config.OASProperties;
import org.tud.oas.services.RoutingService;

@Configuration
public class ORSRoutingManager {
    private String host;

    @Autowired
    public ORSRoutingManager(OASProperties props, RoutingService routing_service) {
        List<String> provider = props.getRouting().getProviders();
        if (provider.contains("ors-api")) {
            Map<String, String> options = props.getRouting().getProviderOptions().get("ors-api");
            if (options == null) {
                this.host = "http://localhost:8082";
            } else if (options.containsKey("url")) {
                this.host = options.get("url");
            } else {
                this.host = "http://localhost:8082";
            }
            routing_service.addRoutingProvider("ors-api", this::getProvider);
        }
    }

    public ORSProvider getProvider() {
        return new ORSProvider(this.host);
    }
}
