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
    [Route("/v1/accessibility/multi")]
    public class MultiCriteriaController
    {
        private ILogger logger;

        public MultiCriteriaController(ILogger<MultiCriteriaController> logger)
        {
            this.logger = logger;
        }

        [HttpPost]
        public async Task<GridResponse> calcMultiCriteriaGrid([FromBody] MultiCriteriaRequest request)
        {
            IRoutingProvider provider = RoutingManager.getRoutingProvider();

            logger.LogDebug("Creating PopulationView");
            IPopulationView view = PopulationManager.getPopulationView(request.getEnvelope(), request.population_type, request.population_indizes);

            logger.LogDebug("Creating GravityAccessibility");
            GravityAccessibility gravity = new GravityAccessibility(view, provider);

            MultiCriteraAccessibility multiCriteria = new MultiCriteraAccessibility(view, gravity);

            logger.LogDebug("Adding Accessbilities");
            foreach (var entry in request.infrastructures) {
                InfrastructureParams value = entry.Value;
                await multiCriteria.addAccessibility(entry.Key, value.facility_locations, value.ranges, value.range_factors, value.infrastructure_weight);
            }
            logger.LogDebug("Finished Adding Accessibilities");

            logger.LogDebug("Building Response");
            multiCriteria.calcAccessibility();
            var response = this.buildResponse(view, multiCriteria.getAccessibilities());
            logger.LogDebug("Finished Building Response Grid");
            return response;
        }

        GridResponse buildResponse(IPopulationView population, Dictionary<int, Dictionary<string, float>> accessibilities)
        {
            List<GridFeature> features = new List<GridFeature>();
            float minx = 1000000000;
            float maxx = -1;
            float miny = 1000000000;
            float maxy = -1;
            List<int> indices = population.getAllPoints();
            foreach (int index in indices) {
                Coordinate p = population.getCoordinate(index, "EPSG:25832");
                Dictionary<string, float> values;
                if (accessibilities.ContainsKey(index)) {
                    values = accessibilities[index];
                }
                else {
                    values = new Dictionary<string, float>();
                    values["multiCritera"] = -9999.0f;
                    values["multiCritera_weighted"] = -9999.0f;
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
                features.Add(new GridFeature((float)p.X, (float)p.Y, values));
            }
            float[] extend = { minx - 50, miny - 50, maxx + 50, maxy + 50 };

            float dx = extend[2] - extend[0];
            float dy = extend[3] - extend[1];
            int[] size = { (int)(dx / 100), (int)(dy / 100) };

            String crs = "EPSG:25832";

            return new GridResponse(features, crs, extend, size);
        }
    }
}