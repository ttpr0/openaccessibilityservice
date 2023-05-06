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

        [HttpPost]
        [Route("grid")]
        public async Task<object> calcGrid([FromBody] AggregateQueryRequest request)
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
                    return new ErrorResponse("queries/aggregate/grid", "failed to get population-view, parameters are invalid");
                }
                IRoutingProvider provider = RoutingManager.getRoutingProvider();

                accessibility = await AggregateQuery.computeAccessibility(request.facility_locations, request.range.Value, view, provider);
            }

            var results = AggregateQuery.computeQuery(request.facility_values, accessibility, request.compute_type);

            var response = this.buildGridResponse(view, results);

            return response;
        }

        GridResponse buildGridResponse(IPopulationView population, float[] values)
        {
            List<GridFeature> features = new List<GridFeature>();
            float minx = 1000000000;
            float maxx = -1;
            float miny = 1000000000;
            float maxy = -1;
            for (int i = 0; i < population.pointCount(); i++) {
                int index = i;
                Coordinate p = population.getCoordinate(index, "EPSG:25832");
                if (p.X < minx) {
                    minx = (float)p.X;
                }
                if (p.X > maxx) {
                    maxx = (float)p.X;
                }
                if (p.Y < miny) {
                    miny = (float)p.Y;
                }
                if (p.Y > maxy) {
                    maxy = (float)p.Y;
                }
                features.Add(new GridFeature((float)p.X, (float)p.Y, new { result = values[i] }));
            }
            float[] extend = { minx - 50, miny - 50, maxx + 50, maxy + 50 };

            float dx = extend[2] - extend[0];
            float dy = extend[3] - extend[1];
            int[] size = { (int)(dx / 100), (int)(dy / 100) };

            string crs = "EPSG:25832";

            return new GridResponse(features, crs, extend, size);
        }
    }
}