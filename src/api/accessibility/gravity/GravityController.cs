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
        public async Task<GridResponse> calcGravityGrid([FromBody] GravityAccessibilityRequest request)
        {
            PopulationContainer population = PopulationManager.getPopulation();
            IRoutingProvider provider = RoutingManager.getRoutingProvider();
            PopulationView view = population.getPopulationView(request.getEnvelope());

            logger.LogDebug("start calculation gravity accessibility");
            long start = Environment.TickCount64;
            GravityAccessibility gravity = new GravityAccessibility(view, provider);
            await gravity.calcAccessibility(request.facility_locations, request.ranges, request.range_factors);
            long end = Environment.TickCount64;
            logger.LogDebug($"finished in {end - start} ms");

            logger.LogDebug("start building response");
            var response = this.buildResponse(view, gravity.getAccessibility());
            logger.LogDebug("response build successfully");
            return response;
        }

        GridResponse buildResponse(PopulationView population, Dictionary<int, Access> accessibility)
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
                if (accessibility.ContainsKey(index)) {
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

            String crs = "EPSG:25832";

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