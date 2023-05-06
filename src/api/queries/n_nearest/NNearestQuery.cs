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
    public class NNearestQuery
    {
        public static async Task<List<RangeRef>[]> computeAccessibility(double[][] locations, List<double> ranges, IPopulationView view, IRoutingProvider provider)
        {
            SimpleAccessibility simple = new SimpleAccessibility(view, provider);

            await simple.calcAccessibility(locations, ranges);

            var accessibilities = simple.getAccessibilities();
            foreach (var rangerefs in accessibilities) {
                rangerefs.Sort((a, b) => {
                    if (a.range > b.range) {
                        return 1;
                    }
                    if (a.range < b.range) {
                        return -1;
                    }
                    return 0;
                });
            }
            return accessibilities;
        }

        public static float[] computeQuery(double[] values, List<RangeRef>[] accessibilities, string computed_type, int count)
        {
            var results = new float[accessibilities.Length];
            for (int i = 0; i < accessibilities.Length; i++) {
                int index = i;
                List<RangeRef> ranges;
                if (accessibilities[index] != null) {
                    ranges = accessibilities[index];
                }
                else {
                    ranges = new List<RangeRef>();
                }
                if (computed_type == "min") {
                    if (ranges.Count < count) {
                        results[i] = -9999;
                    }
                    else {
                        results[i] = (float)ranges.Take(count).Min(item => values[item.index]);
                    }
                }
                if (computed_type == "max") {
                    if (ranges.Count < count) {
                        results[i] = -9999;
                    }
                    else {
                        results[i] = (float)ranges.Take(count).Max(item => values[item.index]);
                    }
                }
                if (computed_type == "median") {
                    if (ranges.Count < count) {
                        results[i] = -9999;
                    }
                    else {
                        var temp = ranges.Take(count).Select(item => values[item.index]).ToList();
                        temp.Sort();
                        if (temp.Count % 2 == 1) {
                            var key = (temp.Count - 1) / 2;
                            results[i] = (float)values[key];
                        }
                        else {
                            var key1 = (temp.Count - 2) / 2;
                            var key2 = (temp.Count - 2) / 2 + 1;
                            results[i] = (float)(values[key1] + values[key2]) / 2;
                        }
                    }
                }
                if (computed_type == "mean") {
                    if (ranges.Count < count) {
                        results[i] = -9999;
                    }
                    else {
                        float sum = 0;
                        for (int j = 0; j < count; j++) {
                            var key = ranges[j].index;
                            sum += (float)values[key];
                        }
                        results[i] = sum / count;
                    }
                }
                if (computed_type == "sum") {
                    if (ranges.Count < count) {
                        results[i] = -9999;
                    }
                    else {
                        float sum = 0;
                        for (int j = 0; j < count; j++) {
                            var key = ranges[j].index;
                            sum += (float)values[key];
                        }
                        results[i] = sum;
                    }
                }
            }

            return results;
        }
    }
}