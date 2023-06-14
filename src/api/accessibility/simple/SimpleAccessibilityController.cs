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
    [Route("/v1/accessibility/simple")]
    public class SimpleAccessibilityController : ControllerBase
    {
        /// <summary>
        /// Calculates simple accessibility.
        /// </summary>
        [HttpPost]
        [ProducesResponseType(200, Type = typeof(SimpleAccessibilityResponse))]
        [ProducesResponseType(400, Type = typeof(ErrorResponse))]
        public async Task<IActionResult> calcSimpleGrid([FromBody] SimpleAccessibilityRequest request)
        {
            IRoutingProvider provider = RoutingManager.getRoutingProvider(request.routing);
            IPopulationView? view = PopulationManager.getPopulationView(request.population);
            if (view == null) {
                return BadRequest(new ErrorResponse("accessibility/gravity/grid", "failed to get population-view, parameters are invalid"));
            }

            var table = await provider.requestKNearest(view, request.facility_locations, request.ranges, 3, "isochrones");

            var response = this.buildResponse(view, table);

            return Ok(new SimpleAccessibilityResponse {
                access = response
            });
        }

        SimpleValue[] buildResponse(IPopulationView population, IKNNTable table)
        {
            var features = new SimpleValue[population.pointCount()];
            for (int i = 0; i < population.pointCount(); i++) {
                int index = i;
                List<(int, float)> ranges = Enumerable.Range(0, 3).Select(item => table.getKNearest(index, item)).ToList();
                SimpleValue value = new SimpleValue(-9999, -9999, -9999);
                if (ranges.Count > 0) {
                    value.first = (int)ranges[0].Item2;
                }
                if (ranges.Count > 1) {
                    value.second = (int)ranges[1].Item2;
                }
                if (ranges.Count > 2) {
                    value.third = (int)ranges[2].Item2;
                }
                features[index] = value;
            }

            return features;
        }
    }
}