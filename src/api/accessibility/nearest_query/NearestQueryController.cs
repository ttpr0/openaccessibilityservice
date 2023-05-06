using System;
using System.Runtime.InteropServices;
using Microsoft.AspNetCore.Http;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using DVAN.Routing;
using System.Threading.Tasks;
using DVAN.Accessibility;
using DVAN.Population;
using NetTopologySuite.Geometries;

namespace DVAN.API
{
    [ApiController]
    [Route("/v1/accessibility/nearest_query")]
    public class NearestQueryController
    {
        static Dictionary<Guid, NearestQuerySession> sessions = new Dictionary<Guid, NearestQuerySession>();

        ILogger logger;

        public NearestQueryController(ILogger<NearestQueryController> logger)
        {
            this.logger = logger;
        }

        [HttpPost]
        public async Task<object> calcGrid([FromBody] NearestQueryRequest request)
        {
            var parameters = NearestQueryParameters.FromRequest(request);
            if (parameters == null) {
                return new ErrorResponse("nearest_query", "invalid parameters");
            }

            IRoutingProvider provider = RoutingManager.getRoutingProvider();
            IPopulationView view = PopulationManager.createPopulationView(parameters.envelope);

            var accessibility = await NearestQuery.computeAccessibility(parameters.facility_locations, parameters.ranges, view, provider);

            Guid id = Guid.NewGuid();
            var session = new NearestQuerySession {
                id = id,
                accessibilities = accessibility,
                parameters = parameters
            };
            sessions[id] = session;

            return new {
                status = "success",
                query_id = id,
            };
        }

        [HttpPost("result")]
        public object computeResult([FromBody] NearestQueryResultRequest request)
        {
            if (!sessions.ContainsKey(request.id)) {
                return new ErrorResponse("nearest_query/result", "no active session found");
            }
            var session = sessions[request.id];
            var accessibilities = session.accessibilities;
            var parameters = session.parameters;

            IPopulationView view = PopulationManager.createPopulationView(parameters.envelope);

            var response = NearestQuery.buildResponse(view, accessibilities, parameters.facility_count);
            return response;
        }

        [HttpPost("compute")]
        public object computeValue([FromBody] NearestQueryComputedRequest request)
        {
            if (!sessions.ContainsKey(request.id)) {
                return new ErrorResponse("nearest_query/compute", "no active session found");
            }
            var session = sessions[request.id];
            var accessibilities = session.accessibilities;

            var computed_values = NearestQuery.buildComputeResponse(accessibilities, request.compute_type, request.range_indizes);
            session.computed_values = computed_values;

            return new {
                values = computed_values
            };
        }

        [HttpPost("statistics")]
        public object computeStatistics([FromBody] NearestQueryStatisticsRequest request)
        {
            if (!sessions.ContainsKey(request.id)) {
                return new ErrorResponse("nearest_query/statistics", "no active session found");
            }
            var session = sessions[request.id];
            if (session.computed_values == null) {
                return new ErrorResponse("nearest_query/statistics", "no current computed result found");
            }
            var parameters = session.parameters;
            var computed_values = session.computed_values;
            var range_max = session.parameters.range_max;
            IPopulationView view = PopulationManager.createPopulationView(parameters.envelope);
            List<int> indizes;
            if (request.envelop == null) {
                indizes = Enumerable.Range(0, view.pointCount()).ToList();
            }
            else {
                var envelope = new Envelope(request.envelop[0], request.envelop[2], request.envelop[1], request.envelop[3]);
                indizes = view.getPointsInEnvelop(envelope);
            }

            var (counts, mean, std, median, min, max) = NearestQuery.buildStatisticsResponse(computed_values, indizes, range_max);

            return new {
                counts = counts,
                mean = mean,
                std = std,
                median = median,
                min = min,
                max = max
            };
        }
    }
}