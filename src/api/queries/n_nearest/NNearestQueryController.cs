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
    [Route("/v1/queries/n_nearest")]
    public class NNearestQueryController : ControllerBase
    {
        static Dictionary<Guid, NNearestQuerySession> sessions = new Dictionary<Guid, NNearestQuerySession>();

        ILogger logger;

        public NNearestQueryController(ILogger<NNearestQueryController> logger)
        {
            this.logger = logger;
        }

        /// <summary>
        /// Calculates nnearest query.
        /// </summary>
        [HttpPost]
        [ProducesResponseType(200, Type = typeof(NNearestQueryResponse))]
        [ProducesResponseType(400, Type = typeof(ErrorResponse))]
        public async Task<IActionResult> calcQuery([FromBody] NNearestQueryRequest request)
        {
            IKNNTable table;
            IPopulationView? view;
            Guid session_id;
            if (request.session_id != null) {
                session_id = request.session_id.Value;
                if (!sessions.ContainsKey(session_id)) {
                    return BadRequest(new ErrorResponse("n_nearest", "no active session found"));
                }
                var session = sessions[session_id];
                table = session.table;
                view = session.population_view;
            }
            else {
                view = PopulationManager.getPopulationView(request.population);
                if (view == null) {
                    return BadRequest(new ErrorResponse("accessibility/gravity/grid", "failed to get population-view, parameters are invalid"));
                }
                IRoutingProvider provider = RoutingManager.getRoutingProvider(request.routing);

                table = await provider.requestKNearest(view, request.facility_locations, request.ranges, request.facility_count, "isochrones");

                session_id = Guid.NewGuid();
                sessions[session_id] = new NNearestQuerySession {
                    id = session_id,
                    table = table,
                    population_view = view
                };
            }

            var results = NNearestQuery.computeQuery(request.facility_values, table, request.compute_type, request.facility_count);

            return Ok(new NNearestQueryResponse {
                result = results,
                session_id = session_id,
            });
        }
    }
}