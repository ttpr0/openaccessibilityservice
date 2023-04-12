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
    [Route("/v1/utility/population")]
    public class PopulationController
    {
        [HttpPost]
        public object storePopulationView([FromBody] PopulationRequest request)
        {
            IPopulationView view;

            if (request.population_locations != null) {
                view = PopulationManager.createPopulationView(request.population_locations, request.population_weights, request.getEnvelope());
            }
            else {
                view = PopulationManager.getPopulationView(request.getEnvelope());
            }

            var id = PopulationManager.storePopulationView(view);
            return new {
                id = id
            };
        }
    }
}