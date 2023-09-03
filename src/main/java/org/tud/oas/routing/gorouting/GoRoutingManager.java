package org.tud.oas.routing.gorouting;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.tud.oas.config.OASProperties;
import org.tud.oas.services.RoutingService;

@Configuration
public class GoRoutingManager {
    private String host;

    @Autowired
    public GoRoutingManager(OASProperties props, RoutingService routing_service) {
        List<String> provider = props.getRouting().getProviders();
        if (provider.contains("routing-api")) {
            Map<String, String> options = props.getRouting().getProviderOptions().get("routing-api");
            if (options == null) {
                this.host = "http://localhost:5002";
            } else if (options.containsKey("url")) {
                this.host = options.get("url");
            } else {
                this.host = "http://localhost:5002";
            }
            routing_service.addRoutingProvider("routing-api", this::getProvider);
        }
    }

    public GoRoutingProvider getProvider() {
        return new GoRoutingProvider(this.host);
    }
}
