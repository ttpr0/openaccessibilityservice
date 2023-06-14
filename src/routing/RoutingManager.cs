using System;
using DVAN.Routing.ORS;

namespace DVAN.Routing
{
    public class RoutingManager
    {
        private static string url;

        public static void setURL(string url)
        {
            RoutingManager.url = url;
        }

        public static IRoutingProvider getRoutingProvider()
        {
            var provider = new ORSProvider(url);

            return provider;
        }

        public static IRoutingProvider getRoutingProvider(RoutingRequestParams? param)
        {
            var provider = new ORSProvider(url);
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
                provider.setOption("isochrone_smoothing", param.isochrone_smoothing.Value);
            }

            return provider;
        }
    }
}