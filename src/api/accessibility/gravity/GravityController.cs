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
    [Route("/v1/accessibility/gravity")]
    public class GravityController
    {
        private ILogger logger;

        public GravityController(ILogger<GravityController> logger)
        {
            this.logger = logger;
        }

        [HttpPost]
        public async Task<object> calcGravity([FromBody] GravityAccessibilityRequest request)
        {
            IPopulationView? view = PopulationManager.getPopulationView(request.population);
            if (view == null) {
                return new ErrorResponse("accessibility/gravity", "failed to get population-view, parameters are invalid");
            }
            IRoutingProvider provider = RoutingManager.getRoutingProvider();

            logger.LogDebug("start calculation gravity accessibility");
            long start = Environment.TickCount64;
            GravityAccessibility gravity = new GravityAccessibility(view, provider);
            await gravity.calcAccessibility(request.facility_locations, request.ranges, request.range_factors);
            long end = Environment.TickCount64;
            logger.LogDebug($"finished in {end - start} ms");

            logger.LogDebug("start building response");
            var response = this.buildResponse(view, gravity.getAccessibility());
            logger.LogDebug("response build successfully");

            return new {
                access = response
            };
        }

        [HttpPost]
        [Route("grid")]
        public async Task<object> calcGravityGrid([FromBody] GravityAccessibilityRequest request)
        {
            IPopulationView? view = PopulationManager.getPopulationView(request.population);
            if (view == null) {
                return new ErrorResponse("accessibility/gravity/grid", "failed to get population-view, parameters are invalid");
            }
            IRoutingProvider provider = RoutingManager.getRoutingProvider();

            logger.LogDebug("start calculation gravity accessibility");
            long start = Environment.TickCount64;
            GravityAccessibility gravity = new GravityAccessibility(view, provider);
            await gravity.calcAccessibility(request.facility_locations, request.ranges, request.range_factors);
            long end = Environment.TickCount64;
            logger.LogDebug($"finished in {end - start} ms");

            logger.LogDebug("start building response");
            var response = this.buildGridResponse(view, gravity.getAccessibility());
            logger.LogDebug("response build successfully");
            return response;
        }

        float[] buildResponse(IPopulationView population, Access[] accessibilities)
        {
            var response = new float[population.pointCount()];
            for (int i = 0; i < population.pointCount(); i++) {
                int index = i;
                float accessibility;
                if (accessibilities[index] != null) {
                    accessibility = accessibilities[index].access;
                }
                else {
                    accessibility = -9999;
                }
                response[i] = accessibility;
            }
            return response;
        }

        GridResponse buildGridResponse(IPopulationView population, Access[] accessibility)
        {
            List<GridFeature> features = new List<GridFeature>();
            float minx = 1000000000;
            float maxx = -1;
            float miny = 1000000000;
            float maxy = -1;
            for (int i = 0; i < population.pointCount(); i++) {
                int index = i;
                Coordinate p = population.getCoordinate(index, "EPSG:25832");
                if (accessibility[index] != null) {
                    Access access = accessibility[index];
                    GravityValue value = new GravityValue(access.access, access.weighted_access);
                    features.Add(new GridFeature((float)p.X, (float)p.Y, value));
                }
                else {
                    GravityValue value = new GravityValue(-9999, -9999);
                    features.Add(new GridFeature((float)p.X, (float)p.Y, value));
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
            }
            float[] extend = { minx - 50, miny - 50, maxx + 50, maxy + 50 };

            float dx = extend[2] - extend[0];
            float dy = extend[3] - extend[1];
            int[] size = { (int)(dx / 100), (int)(dy / 100) };

            string crs = "EPSG:25832";

            return new GridResponse(features, crs, extend, size);
        }

        class GravityValue
        {
            public float unweighted { get; set; }
            public float weighted { get; set; }

            public GravityValue(float unweighted, float weighted)
            {
                this.unweighted = unweighted;
                this.weighted = weighted;
            }
        }
    }
}