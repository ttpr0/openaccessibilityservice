using System;
using System.Runtime.InteropServices;
using Microsoft.AspNetCore.Http;
using System.Collections;
using System.Collections.Generic;
using System.Text.Json;
using System.Linq;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using DVAN.Routing;
using DVAN.Accessibility;
using DVAN.Population;
using System.Threading.Tasks;
using NetTopologySuite.Geometries;

namespace DVAN.API
{
    [ApiController]
    [Route("/v1/fca")]
    public class FCAController
    {
        [HttpPost]
        public async Task<object> calcFCA([FromBody] FCARequest request)
        {
            IPopulationView view;
            if (request.population_id != null) {
                view = PopulationManager.getStoredPopulationView(request.population_id.Value);
                if (view == null) {
                    if (request.population_locations != null) {
                        view = PopulationManager.createPopulationView(request.population_locations, request.population_weights, request.getEnvelope());
                    }
                    else {
                        view = PopulationManager.getPopulationView(request.getEnvelope());
                    }
                }
            }
            else if (request.population_locations != null) {
                view = PopulationManager.createPopulationView(request.population_locations, request.population_weights, request.getEnvelope());
            }
            else {
                view = PopulationManager.getPopulationView(request.getEnvelope());
            }
            IRoutingProvider provider = RoutingManager.getRoutingProvider();

            var weights = await Simple2SFCA.calc2SFCA(view, request.facility_locations, request.ranges, request.range_factors, provider, request.mode);

            float max_weight = 0;
            foreach (float w in weights.Values) {
                if (w > max_weight) {
                    max_weight = w;
                }
            }
            float factor = 100 / max_weight;

            var response = this.buildResponse(view, weights, factor);
            return new {
                access = response
            };
        }

        [HttpPost]
        [Route("grid")]
        public async Task<GridResponse> calcFCAGrid([FromBody] FCARequest request)
        {
            IPopulationView view;
            if (request.population_id != null) {
                view = PopulationManager.getStoredPopulationView(request.population_id.Value);
                if (view == null) {
                    if (request.population_locations != null) {
                        view = PopulationManager.createPopulationView(request.population_locations, request.population_weights, request.getEnvelope());
                    }
                    else {
                        view = PopulationManager.getPopulationView(request.getEnvelope());
                    }
                }
            }
            else if (request.population_locations != null) {
                view = PopulationManager.createPopulationView(request.population_locations, request.population_weights, request.getEnvelope());
            }
            else {
                view = PopulationManager.getPopulationView(request.getEnvelope());
            }
            IRoutingProvider provider = RoutingManager.getRoutingProvider();

            var weights = await Simple2SFCA.calc2SFCA(view, request.facility_locations, request.ranges, request.range_factors, provider, request.mode);

            float max_weight = 0;
            foreach (float w in weights.Values) {
                if (w > max_weight) {
                    max_weight = w;
                }
            }
            float factor = 100 / max_weight;

            var response = this.buildGridResponse(view, weights, factor);
            return response;
        }

        float[] buildResponse(IPopulationView population, Dictionary<int, float> accessibilities, float factor)
        {
            List<int> indices = population.getAllPoints();
            var response = new float[indices.Count];
            for (int i = 0; i < indices.Count; i++) {
                int index = indices[i];
                float accessibility;
                if (accessibilities.TryGetValue(index, out float value)) {
                    accessibility = value * factor;
                }
                else {
                    accessibility = -9999;
                }
                response[i] = accessibility;
            }
            return response;
        }

        GridResponse buildGridResponse(IPopulationView population, Dictionary<int, float> accessibilities, float factor)
        {
            List<GridFeature> features = new List<GridFeature>();
            float minx = 1000000000;
            float maxx = -1;
            float miny = 1000000000;
            float maxy = -1;
            List<int> indices = population.getAllPoints();
            for (int i = 0; i < indices.Count; i++) {
                int index = indices[i];
                Coordinate p = population.getCoordinate(index, "EPSG:25832");
                float accessibility;
                if (accessibilities.ContainsKey(index)) {
                    accessibility = accessibilities[index] * factor;
                }
                else {
                    accessibility = -9999;
                }
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
                FCAValue value = new FCAValue(accessibility);
                features.Add(new GridFeature((float)p.X, (float)p.Y, value));
            }
            float[] extend = { minx - 50, miny - 50, maxx + 50, maxy + 50 };

            float dx = extend[2] - extend[0];
            float dy = extend[3] - extend[1];
            int[] size = { (int)(dx / 100), (int)(dy / 100) };

            string crs = "EPSG:25832";

            return new GridResponse(features, crs, extend, size);
        }

        class FCAValue
        {
            public float accessibility { get; set; }

            public FCAValue(float accessibility)
            {
                this.accessibility = accessibility;
            }
        }
    }
}