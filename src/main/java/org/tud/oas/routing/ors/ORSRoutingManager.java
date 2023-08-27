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
    public ORSRoutingManager(OASProperties props) {
        List<String> provider = props.getRouting().getProviders();
        if (provider.contains("ors_api")) {
            Map<String, String> options = props.getRouting().getProviderOptions().get("ors_api");
            if (options == null) {
                this.host = "localhost:8082";
            } else if (options.containsKey("host")) {
                this.host = options.get("host");
            } else {
                this.host = "localhost:8082";
            }
            RoutingService.addRoutingProvider("ors_api", this::getProvider);
        }
    }

    public ORSProvider getProvider() {
        return new ORSProvider(this.host);
    }
}
