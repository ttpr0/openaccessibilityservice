package org.tud.oas.services;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tud.oas.config.OASProperties;
import org.tud.oas.requests.RoutingRequestParams;
import org.tud.oas.routing.IRoutingProvider;

@Service
public class RoutingService {
    static final Logger logger = LoggerFactory.getLogger(RoutingService.class);

    private Map<String, Supplier<IRoutingProvider>> providers;
    private String defaultProvider;

    @Autowired
    public RoutingService(OASProperties props) {
        String default_provider = props.getRouting().getDefaultProvider();
        this.defaultProvider = default_provider;
        this.providers = new HashMap<>();
    }

    public void setDefaultProvider(String name) {
        this.defaultProvider = name;
    }

    public void addRoutingProvider(String name, Supplier<IRoutingProvider> cls) {
        this.providers.put(name, cls);
    }

    public IRoutingProvider getDefaultProvider() {
        IRoutingProvider provider = this.providers.get(this.defaultProvider).get();
        return provider;
    }

    public IRoutingProvider getRoutingProvider(RoutingRequestParams param) {
        if (param == null) {
            return this.providers.get(this.defaultProvider).get();
        }
        IRoutingProvider provider;
        if (param.routing_provider == null || this.providers.containsKey(param.routing_provider)) {
            provider = this.providers.get(this.defaultProvider).get();
        } else {
            provider = this.providers.get(param.routing_provider).get();
        }

        if (param.profile != null) {
            provider.setProfile(param.profile);
        }
        if (param.range_type != null) {
            provider.setRangeType(param.range_type);
        }
        if (param.location_type != null) {
            provider.setParameter("location_type", param.location_type);
        }
        if (param.isochrone_smoothing != null) {
            provider.setParameter("isochrone_smoothing", param.isochrone_smoothing);
        }

        return provider;
    }
}
