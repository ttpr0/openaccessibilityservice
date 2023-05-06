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
    [Route("/v1/queries/aggregate")]
    public class AggregateQueryController
    {
        static Dictionary<Guid, AggregateQuerySession> sessions = new Dictionary<Guid, AggregateQuerySession>();

        ILogger logger;

        public AggregateQueryController(ILogger<AggregateQueryController> logger)
        {
            this.logger = logger;
        }

        [HttpPost]
        public async Task<object> calcQuery([FromBody] AggregateQueryRequest request)
        {
            List<int>[] accessibility;
            IPopulationView? view;
            if (request.session_id != null) {
                var session_id = request.session_id.Value;
                if (!sessions.ContainsKey(session_id)) {
                    return new ErrorResponse("n_nearest", "no active session found");
                }
                var session = sessions[session_id];
                accessibility = session.accessibilities;
                view = session.population_view;
            }
            else {
                view = PopulationManager.getPopulationView(request.population);
                if (view == null) {
                    return new ErrorResponse("queries/aggregate", "failed to get population-view, parameters are invalid");
                }
                IRoutingProvider provider = RoutingManager.getRoutingProvider();

                accessibility = await AggregateQuery.computeAccessibility(request.facility_locations, request.range.Value, view, provider);
            }

            Guid id = Guid.NewGuid();

            sessions[id] = new AggregateQuerySession {
                id = id,
                accessibilities = accessibility,
                population_view = view
            };

            var results = AggregateQuery.computeQuery(request.facility_values, accessibility, request.compute_type);

            return new {
                result = results,
                session_id = id
            };
        }
    }
}