package org.tud.oas.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoutingManager {
    static final Logger logger = LoggerFactory.getLogger(RoutingManager.class);
    private static IRoutingProvider provider;

    public static void addRoutingProvider(IRoutingProvider provider) throws Exception {
        RoutingManager.provider = provider;
    }

    public static IRoutingProvider getRoutingProvider() {
        return RoutingManager.provider;
    }
}
