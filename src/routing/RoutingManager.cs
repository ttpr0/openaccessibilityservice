using System;

namespace DVAN.Routing
{
    public class RoutingManager
    {
        private static IRoutingProvider provider;

        public static void addRoutingProvider(IRoutingProvider provider)
        {
            RoutingManager.provider = provider;
        }

        public static IRoutingProvider getRoutingProvider()
        {
            return RoutingManager.provider;
        }
    }
}