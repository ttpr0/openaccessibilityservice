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

    static Map<String, Supplier<IRoutingProvider>> providers;
    static String defaultProvider;

    public static void setDefaultProvider(String name) {
        RoutingService.defaultProvider = name;
    }

    public static void addRoutingProvider(String name, Supplier<IRoutingProvider> cls) {
        RoutingService.providers.put(name, cls);
    }

    public static IRoutingProvider getDefaultProvider() {
        return RoutingService.providers.get(RoutingService.defaultProvider).get();
    }

    public static IRoutingProvider getRoutingProvider(String name) {
        return RoutingService.providers.get(name).get();
    }

    @Autowired
    public RoutingService(OASProperties props) {
        String default_provider = props.getRouting().getDefaultProvider();
        RoutingService.defaultProvider = default_provider;
        RoutingService.providers = new HashMap<>();
    }

    public IRoutingProvider getRoutingProvider() {
        IRoutingProvider provider = RoutingService.getDefaultProvider();
        return provider;
    }

    public IRoutingProvider getRoutingProvider(RoutingRequestParams param) {
        IRoutingProvider provider = RoutingService.getDefaultProvider();

        if (param == null) {
            return provider;
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
