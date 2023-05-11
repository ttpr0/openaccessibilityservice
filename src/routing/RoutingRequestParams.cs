using System;

namespace DVAN.Routing
{
    /// <summary>
    /// Parameters for the routing.
    /// Parameters will be used for queries on routing backend.
    /// All Parameters are optional, is usefull defaults will be set.
    /// </summary>
    public class RoutingRequestParams
    {
        //*************************************
        // standard routing params
        //*************************************
        /// <summary>
        /// Routing profile to be used (e.g. driving-car, ...).
        /// </summary>
        /// <example>driving-car</example>
        public string? profile { get; set; }

        /// <summary>
        /// Routing metric (travel-time or distance).
        /// </summary>
        /// <example>time</example>
        public string? range_type { get; set; }

        //*************************************
        // additional routing params
        //*************************************
        /// <summary>
        /// Sets weather borders should be avoided (cross country).
        /// </summary>
        /// <example>all</example>
        public string? avoid_borders { get; set; }

        /// <summary>
        /// Sets which road segments should be avoided.
        /// </summary>
        /// <example>["highway", "ferries"]</example>
        public string[]? avoid_features { get; set; }

        /// <summary>
        /// Sets an area to avid while routing.
        /// Polygon or MultiPolygon formatted as GeoJSON.
        /// </summary>
        /// <example>{"type": "Polygon", "coordinates": [[9.1, 50.1], [9.9, 50.1], [9.4, 50.8]]}</example>
        public object? avoid_polygons { get; set; }

        //*************************************
        // isochrone params
        //*************************************
        /// <summary>
        /// Sets weather input locations should be used as origin or destination of travel.
        /// </summary>
        /// <example>start</example>
        public string? location_type { get; set; }

        /// <summary>
        /// Sets how much isochrones should be smoothed.
        /// High values lead to a high degree of smoothing.
        /// </summary>
        /// <example>25</example>
        public float? isochrone_smoothing { get; set; }
    }
}
