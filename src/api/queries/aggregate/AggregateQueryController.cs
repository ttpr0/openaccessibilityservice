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
    public class AggregateQueryController : ControllerBase
    {
        static Dictionary<Guid, AggregateQuerySession> sessions = new Dictionary<Guid, AggregateQuerySession>();

        ILogger logger;

        public AggregateQueryController(ILogger<AggregateQueryController> logger)
        {
            this.logger = logger;
        }

        /// <summary>
        /// Calculates aggregate query.
        /// </summary>
        [HttpPost]
        [ProducesResponseType(200, Type = typeof(AggregateQueryResponse))]
        [ProducesResponseType(400, Type = typeof(ErrorResponse))]
        public async Task<IActionResult> calcQuery([FromBody] AggregateQueryRequest request)
        {
            ICatchment catchment;
            IPopulationView? view;
            if (request.session_id != null) {
                var session_id = request.session_id.Value;
                if (!sessions.ContainsKey(session_id)) {
                    return BadRequest(new ErrorResponse("n_nearest", "no active session found"));
                }
                var session = sessions[session_id];
                catchment = session.catchment;
                view = session.population_view;
            }
            else {
                view = PopulationManager.getPopulationView(request.population);
                if (view == null) {
                    return BadRequest(new ErrorResponse("queries/aggregate", "failed to get population-view, parameters are invalid"));
                }
                IRoutingProvider provider = RoutingManager.getRoutingProvider(request.routing);

                catchment = await provider.requestCatchment(view, request.facility_locations, request.range.Value, "isochrones");
            }

            Guid id = Guid.NewGuid();

            sessions[id] = new AggregateQuerySession {
                id = id,
                catchment = catchment,
                population_view = view
            };

            var results = AggregateQuery.computeQuery(request.facility_values, catchment, request.compute_type);

            return Ok(new AggregateQueryResponse {
                result = results,
                session_id = id
            });
        }
    }
}