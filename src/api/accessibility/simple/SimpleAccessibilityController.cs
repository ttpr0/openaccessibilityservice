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
    public class SimpleAccessibilityController
    {
        [HttpPost]
        public async Task<GridResponse> calcSimpleGrid([FromBody] SimpleAccessibilityRequest request)
        {
            PopulationContainer population = PopulationManager.getPopulation();
            IRoutingProvider provider = RoutingManager.getRoutingProvider();
            PopulationView view = population.getPopulationView(request.getEnvelope());

            SimpleAccessibility simple = new SimpleAccessibility(view, provider);

            await simple.calcAccessibility(request.facility_locations, request.ranges);

            var response = this.buildResponse(view, simple.getAccessibilities());

            return response;
        }

        GridResponse buildResponse(PopulationView population, Dictionary<int, List<RangeRef>> accessibilities)
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
                List<RangeRef> ranges;
                if (accessibilities.ContainsKey(index)) {
                    ranges = accessibilities[index];
                }
                else {
                    ranges = new List<RangeRef>();
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
                features.Add(new GridFeature((float)p.X, (float)p.Y, value));
            }
            float[] extend = { minx - 50, miny - 50, maxx + 50, maxy + 50 };

            float dx = extend[2] - extend[0];
            float dy = extend[3] - extend[1];
            int[] size = { (int)(dx / 100), (int)(dy / 100) };

            String crs = "EPSG:25832";

            return new GridResponse(features, crs, extend, size);
        }

        class SimpleValue
        {
            public int first { get; set; }
            public int second { get; set; }
            public int third { get; set; }

            public SimpleValue(int first, int second, int third)
            {
                this.first = first;
                this.second = second;
                this.third = third;
            }
        }
    }
}