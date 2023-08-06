package org.tud.oas.routing;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoutingManager {
    static final Logger logger = LoggerFactory.getLogger(RoutingManager.class);
    private static Supplier<IRoutingProvider> cls;

    public static void setRoutingProvider(Supplier<IRoutingProvider> cls) {
        RoutingManager.cls = cls;
    }

    public static IRoutingProvider getRoutingProvider() {
        IRoutingProvider provider = RoutingManager.cls.get();
        return provider;
    }

    public static IRoutingProvider getRoutingProvider(RoutingRequestParams param) {
        IRoutingProvider provider = RoutingManager.cls.get();

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
            provider.setOption("location_type", param.location_type);
        }
        if (param.isochrone_smoothing != null) {
            provider.setOption("isochrone_smoothing", param.isochrone_smoothing);
        }

        return provider;
    }
}
