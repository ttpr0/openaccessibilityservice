package org.tud.oas.services;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tud.oas.requests.RoutingRequestParams;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.embedded.EmbeddedRoutingProvider;
import org.tud.oas.routing.ors.ORSProvider;

@Service
public class RoutingService {
    static final Logger logger = LoggerFactory.getLogger(RoutingService.class);
    static Supplier<IRoutingProvider> cls;

    public static void setRoutingProvider(Supplier<IRoutingProvider> cls) {
        RoutingService.cls = cls;
    }

    public IRoutingProvider getRoutingProvider() {
        IRoutingProvider provider = RoutingService.cls.get();
        return provider;
    }

    public IRoutingProvider getRoutingProvider(RoutingRequestParams param) {
        IRoutingProvider provider = RoutingService.cls.get();

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
