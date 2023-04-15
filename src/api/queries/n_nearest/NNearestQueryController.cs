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
    public class NNearestQueryController
    {
        static Dictionary<Guid, NNearestQuerySession> sessions = new Dictionary<Guid, NNearestQuerySession>();

        ILogger logger;

        public NNearestQueryController(ILogger<NNearestQueryController> logger)
        {
            this.logger = logger;
        }

        [HttpPost]
        public async Task<object> calcQuery([FromBody] NNearestQueryRequest request)
        {
            Dictionary<int, List<RangeRef>> accessibility;
            IPopulationView view;
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
                if (request.population_id != null) {
                    view = PopulationManager.getStoredPopulationView(request.population_id.Value);
                    if (view == null) {
                        if (request.population_locations != null) {
                            view = PopulationManager.createPopulationView(request.population_locations, request.getEnvelope());
                        }
                        else {
                            view = PopulationManager.getPopulationView(request.getEnvelope());
                        }
                    }
                }
                else if (request.population_locations != null) {
                    view = PopulationManager.createPopulationView(request.population_locations, request.getEnvelope());
                }
                else {
                    view = PopulationManager.getPopulationView(request.getEnvelope());
                }
                IRoutingProvider provider = RoutingManager.getRoutingProvider();

                accessibility = await NNearestQuery.computeAccessibility(request.facility_locations, request.ranges, view, provider);
            }

            Guid id = Guid.NewGuid();
            List<int> population_indices = view.getAllPoints();

            sessions[id] = new NNearestQuerySession {
                id = id,
                accessibilities = accessibility,
                population_view = view
            };

            var results = NNearestQuery.computeQuery(population_indices, request.facility_values, accessibility, request.compute_type, request.facility_count);

            return new {
                result = results,
                session_id = id
            };
        }

        [HttpPost]
        [Route("grid")]
        public async Task<object> calcGrid([FromBody] NNearestQueryRequest request)
        {
            Dictionary<int, List<RangeRef>> accessibility;
            IPopulationView view;
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
                if (request.population_id != null) {
                    view = PopulationManager.getStoredPopulationView(request.population_id.Value);
                    if (view == null) {
                        if (request.population_locations != null) {
                            view = PopulationManager.createPopulationView(request.population_locations, request.getEnvelope());
                        }
                        else {
                            view = PopulationManager.getPopulationView(request.getEnvelope());
                        }
                    }
                }
                else if (request.population_locations != null) {
                    view = PopulationManager.createPopulationView(request.population_locations, request.getEnvelope());
                }
                else {
                    view = PopulationManager.getPopulationView(request.getEnvelope());
                }
                IRoutingProvider provider = RoutingManager.getRoutingProvider();

                accessibility = await NNearestQuery.computeAccessibility(request.facility_locations, request.ranges, view, provider);
            }

            List<int> population_indices = view.getAllPoints();
            var results = NNearestQuery.computeQuery(population_indices, request.facility_values, accessibility, request.compute_type, request.facility_count);

            var response = this.buildGridResponse(population_indices, view, results);

            return response;
        }

        GridResponse buildGridResponse(List<int> indices, IPopulationView population, float[] values)
        {
            List<GridFeature> features = new List<GridFeature>();
            float minx = 1000000000;
            float maxx = -1;
            float miny = 1000000000;
            float maxy = -1;
            for (int i = 0; i < indices.Count; i++) {
                int index = indices[i];
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