package org.tud.oas.routing.distance;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.tud.oas.config.OASProperties;
import org.tud.oas.services.RoutingService;

@Configuration
public class DistanceManager {
    private String dist_type;
    private double avg_speed;

    @Autowired
    public DistanceManager(OASProperties props, RoutingService routing_service) {
        List<String> provider = props.getRouting().getProviders();
        if (provider.contains("distance")) {
            this.dist_type = "eucledian";
            this.avg_speed = 50;
            Map<String, String> options = props.getRouting().getProviderOptions().get("distance");
            if (options != null) {
                if (options.containsKey("metric")) {
                    this.dist_type = options.get("metric");
                }
                if (options.containsKey("speed")) {
                    this.avg_speed = Double.parseDouble(options.get("speed"));
                }
            }
            routing_service.addRoutingProvider("distance", this::getProvider);
        }
    }

    public DistanceProvider getProvider() {
        return new DistanceProvider(this.dist_type, this.avg_speed);
    }
}
