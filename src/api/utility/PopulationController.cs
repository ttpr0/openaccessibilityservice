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
        [Route("store")]
        public object storePopulationView([FromBody] PopulationStoreRequest request)
        {
            IPopulationView? view = PopulationManager.getPopulationView(request.population);
            if (view == null) {
                return new ErrorResponse("utility/population/store", "failed to get population-view, parameters are invalid");
            }

            var id = PopulationManager.storePopulationView(view);
            return new {
                id = id
            };
        }

        [HttpPost]
        [Route("get")]
        public object getPopulationView([FromBody] PopulationGetRequest request)
        {
            IPopulationView? view;

            if (request.population_id != null) {
                view = PopulationManager.getStoredPopulationView(request.population_id.Value);
                if (view == null) {
                    return new ErrorResponse("utility/population/get", "no stored population-view found");
                }
            }
            else {
                view = PopulationManager.getPopulationView(request.population);
                if (view == null) {
                    return new ErrorResponse("utility/population/get", "failed to get population-view, parameters are invalid");
                }
            }

            var locations = new List<double[]>();
            var weights = new List<double>();
            for (int i = 0; i < view.pointCount(); i++) {
                int index = i;
                var point = view.getCoordinate(index);
                var weight = view.getPopulation(index);

                locations.Add(new double[] { point.X, point.Y });
                weights.Add(weight);
            }

            return new {
                locations = locations,
                weights = weights
            };
        }
    }
}