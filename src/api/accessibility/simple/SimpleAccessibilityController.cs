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
            IRoutingProvider provider = RoutingManager.getRoutingProvider();
            IPopulationView? view = PopulationManager.getPopulationView(request.population);
            if (view == null) {
                return BadRequest(new ErrorResponse("accessibility/gravity/grid", "failed to get population-view, parameters are invalid"));
            }

            SimpleAccessibility simple = new SimpleAccessibility(view, provider);

            await simple.calcAccessibility(request.facility_locations, request.ranges);

            var response = this.buildResponse(view, simple.getAccessibilities());

            return Ok(new SimpleAccessibilityResponse {
                access = response
            });
        }

        SimpleValue[] buildResponse(IPopulationView population, List<RangeRef>[] accessibilities)
        {
            var features = new SimpleValue[population.pointCount()];
            for (int i = 0; i < population.pointCount(); i++) {
                int index = i;
                List<RangeRef> ranges;
                if (accessibilities[index] != null) {
                    ranges = accessibilities[index];
                }
                else {
                    ranges = new List<RangeRef>();
                }
                ranges.Sort((RangeRef a, RangeRef b) => {
                    return (int)(a.range - b.range);
                });
                SimpleValue value = new SimpleValue(-9999, -9999, -9999);
                if (ranges.Count > 0) {
                    value.first = (int)ranges[0].range;
                }
                if (ranges.Count > 1) {
                    value.second = (int)ranges[1].range;
                }
                if (ranges.Count > 2) {
                    value.third = (int)ranges[2].range;
                }
                features[index] = value;
            }

            return features;
        }
    }
}