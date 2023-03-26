using System;
using System.Collections.Generic;
using DVAN.Accessibility;
using NetTopologySuite.Geometries;

namespace DVAN.API
{
    class NearestQueryParameters
    {
        public double[][] facility_locations { get; set; }

        public int facility_count { get; set; }

        public string range_type { get; set; }

        public int range_max { get; set; }

        public List<double>? ranges { get; set; }

        public Envelope? envelope { get; set; }

        public static NearestQueryParameters? FromRequest(NearestQueryRequest request)
        {
            var parameters = new NearestQueryParameters();
            parameters.facility_locations = request.facility_locations;
            parameters.facility_count = request.facility_count;
            if (request.range_type == "continuus") {
                parameters.range_type = request.range_type;
                if (request.range_max == null) {
                    return null;
                }
                parameters.range_max = (int)request.range_max;
            } else if (request.range_type == "discrete") {
                parameters.range_type = request.range_type;
                if (request.ranges == null) {
                    return null;
                }
                request.ranges.Sort();
                parameters.range_max = (int)request.ranges[^1];
                parameters.ranges = request.ranges;
            }
            if (request.envelop != null) {
                parameters.envelope = new Envelope(request.envelop[0], request.envelop[2], request.envelop[1], request.envelop[3]);
            }
            return parameters;
        }
    }
}