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
    public class FCAController : ControllerBase
    {
        /// <summary>
        /// Calculates simple floating catchment area.
        /// </summary>
        [HttpPost]
        [ProducesResponseType(200, Type = typeof(FCAResponse))]
        [ProducesResponseType(400, Type = typeof(ErrorResponse))]
        public async Task<IActionResult> calcFCA([FromBody] FCARequest request)
        {
            IPopulationView? view = PopulationManager.getPopulationView(request.population);
            if (view == null) {
                return BadRequest(new ErrorResponse("accessibility/gravity/grid", "failed to get population-view, parameters are invalid"));
            }
            IRoutingProvider provider = RoutingManager.getRoutingProvider();

            var weights = await Enhanced2SFCA.calc2SFCA(view, request.facility_locations, request.ranges, request.range_factors, provider, request.mode);

            float max_weight = 0;
            foreach (float w in weights) {
                if (w > max_weight) {
                    max_weight = w;
                }
            }
            float factor = 100 / max_weight;

            var response = this.buildResponse(view, weights, factor);
            return Ok(new FCAResponse {
                access = response
            });
        }

        float[] buildResponse(IPopulationView population, float[] accessibilities, float factor)
        {
            for (int i = 0; i < accessibilities.Length; i++) {
                float accessibility = accessibilities[i];
                if (accessibility != 0) {
                    accessibility = accessibility * factor;
                }
                else {
                    accessibility = -9999;
                }
                accessibilities[i] = accessibility;
            }
            return accessibilities;
        }
    }
}