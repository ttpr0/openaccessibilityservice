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
    public class GravityController : ControllerBase
    {
        private ILogger logger;

        public GravityController(ILogger<GravityController> logger)
        {
            this.logger = logger;
        }

        /// <summary>
        /// Calculates simple gravity accessibility.
        /// </summary>
        [HttpPost]
        [ProducesResponseType(200, Type = typeof(GravityResponse))]
        [ProducesResponseType(400, Type = typeof(ErrorResponse))]
        public async Task<IActionResult> calcGravity([FromBody] GravityAccessibilityRequest request)
        {
            IPopulationView? view = PopulationManager.getPopulationView(request.population);
            if (view == null) {
                return BadRequest(new ErrorResponse("accessibility/gravity", "failed to get population-view, parameters are invalid"));
            }
            IRoutingProvider provider = RoutingManager.getRoutingProvider(request.routing);

            logger.LogDebug("start calculation gravity accessibility");
            long start = Environment.TickCount64;
            GravityAccessibility gravity = new GravityAccessibility();
            var access = await gravity.calcAccessibility(view, request.facility_locations, request.ranges, request.range_factors, provider);
            long end = Environment.TickCount64;
            logger.LogDebug($"finished in {end - start} ms");

            logger.LogDebug("start building response");
            var response = this.buildResponse(view, access);
            logger.LogDebug("response build successfully");

            return Ok(new GravityResponse {
                access = response
            });
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
    }
}