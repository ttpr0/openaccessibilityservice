using System;
using System.Runtime.InteropServices;
using Microsoft.AspNetCore.Http;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using DVAN.Routing;
using DVAN.FCA;
using DVAN.Population;
using System.Threading.Tasks;

namespace DVAN.API
{
    [ApiController]
    [Route("/v1/fca")]
    public class API 
    {

        [HttpPost]
        public async Task<FCAGeoJSONResponse> calculateFCA([FromBody] FCARequest request)
        {
            PopulationContainer population = PopulationManager.getPopulation();
            IRoutingProvider provider = RoutingManager.getRoutingProvider();

            float[] weights = await Simple2SFCA.calc2SFCA(population, request.facility_locations, request.ranges, request.range_factors, provider);
        
            float max_weight = 0;
            foreach (float w in weights) {
                if (w>max_weight) {
                    max_weight = w;
                }
            }
            float factor = 100/max_weight;
            for (int i=0; i<weights.Length; i++) {
                if (weights[i] != -1) {
                    weights[i] = weights[i]*factor;
                }
            }
            return new FCAGeoJSONResponse(population, weights);
        }
    }
}