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

namespace DVAN.API
{
    [ApiController]
    [Route("/v1/accessibility")]
    public class AccessibilityController {

        [HttpPost]
        public async Task<GridResponse> calcGrid([FromBody] SimpleAccessibilityRequest request)
        {
            return await this.calcSimpleGrid(request);
        }

        [HttpPost("simple")]
        public async Task<GridResponse> calcSimpleGrid([FromBody] SimpleAccessibilityRequest request)
        {
            PopulationContainer population = PopulationManager.getPopulation();
            IRoutingProvider provider = RoutingManager.getRoutingProvider();

            SimpleAccessibility simple = new SimpleAccessibility(population, provider);

            await simple.calcAccessibility(request.getLocations(), request.getRanges());
            
            return simple.buildResponse();
        }

        [HttpPost("gravity")]
        public async Task<GridResponse> calcGravityGrid([FromBody] GravityAccessibilityRequest request)
        {
            PopulationContainer population = PopulationManager.getPopulation();
            IRoutingProvider provider = RoutingManager.getRoutingProvider();
            PopulationView view = population.getPopulationView(request.getEnvelop());

            long start = Environment.TickCount64;
            GravityAccessibility gravity = new GravityAccessibility(view, provider);

            await gravity.calcAccessibility(request.getLocations(), request.getRanges(), request.getFactors());
            long end = Environment.TickCount64;

            Console.WriteLine("time: " + (end - start));

            return gravity.buildResponse();
        }

        [HttpPost("multi")]
        public async Task<GridResponse> calcMultiCriteriaGrid([FromBody] MultiCriteriaRequest request) 
        {
            PopulationContainer population = PopulationManager.getPopulation();
            IRoutingProvider provider = RoutingManager.getRoutingProvider();
            PopulationView view = population.getPopulationView(request.getEnvelope());

            view.setPopulationType(request.getPopulationType());
            view.setPopulationIndizes(request.getPopulationIndizes());

            GravityAccessibility gravity = new GravityAccessibility(view, provider);

            MultiCriteraAccessibility multiCriteria = new MultiCriteraAccessibility(view, gravity);

            foreach (var entry in request.getInfrastructures()) {
                InfrastructureParams value = entry.Value;
                await multiCriteria.addAccessibility(entry.Key, value.facility_locations, value.ranges, value.range_factors, value.infrastructure_weight);
            }
            multiCriteria.calcAccessibility();
            var response = multiCriteria.buildResponse();
            return response;
        }
    }
}