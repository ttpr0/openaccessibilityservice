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
    public class NearestQuery
    {
        public static async Task<Dictionary<int, List<RangeRef>>> computeAccessibility(double[][] locations, List<double> ranges, IPopulationView view, IRoutingProvider provider)
        {
            SimpleAccessibility simple = new SimpleAccessibility(view, provider);

            await simple.calcAccessibility(locations, ranges);

            return simple.getAccessibilities();
        }

        public static GridResponse buildGridResponse(List<int> indices, IPopulationView population, Dictionary<int, List<RangeRef>> accessibilities, int count)
        {
            List<GridFeature> features = new List<GridFeature>();
            float minx = 1000000000;
            float maxx = -1;
            float miny = 1000000000;
            float maxy = -1;
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
                accessibilities[index] = ranges;
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
                var value = new Dictionary<string, int>(count);
                for (int j = 1; j <= count; j++) {
                    var key = Convert.ToString(j);
                    if (ranges.Count >= j) {
                        value[key] = (int)ranges[j - 1].range;
                    }
                    else {
                        value[key] = -9999;
                    }
                }
                value["index"] = i;
                features.Add(new GridFeature((float)p.X, (float)p.Y, value));
            }
            float[] extend = { minx - 50, miny - 50, maxx + 50, maxy + 50 };

            float dx = extend[2] - extend[0];
            float dy = extend[3] - extend[1];
            int[] size = { (int)(dx / 100), (int)(dy / 100) };

            String crs = "EPSG:25832";

            return new GridResponse(features, crs, extend, size);
        }

        public static int[] buildComputeResponse(List<int> indices, Dictionary<int, List<RangeRef>> accessibilities, string computed_type, List<int> range_indices)
        {
            range_indices.Sort((int a, int b) => a - b);
            var values = new int[indices.Count];
            for (int i = 0; i < indices.Count; i++) {
                int index = indices[i];
                List<RangeRef> ranges;
                if (accessibilities.ContainsKey(index)) {
                    ranges = accessibilities[index];
                }
                else {
                    ranges = new List<RangeRef>();
                }
                if (computed_type == "min") {
                    var key = range_indices[0];
                    if (ranges.Count > key) {
                        values[i] = (int)ranges[key].range;
                    }
                    else {
                        values[i] = -9999;
                    }
                }
                if (computed_type == "max") {
                    var key = range_indices[^1];
                    if (ranges.Count > key) {
                        values[i] = (int)ranges[key].range;
                    }
                    else {
                        values[i] = -9999;
                    }
                }
                if (computed_type == "median") {
                    if (range_indices.Count % 2 == 1) {
                        var key = range_indices[(range_indices.Count - 1) / 2];
                        if (ranges.Count > key) {
                            values[i] = (int)ranges[key].range;
                        }
                        else {
                            values[i] = -9999;
                        }
                    }
                    else {
                        var key1 = range_indices[(range_indices.Count - 2) / 2];
                        var key2 = range_indices[(range_indices.Count - 2) / 2 + 1];
                        if (ranges.Count > key2) {
                            values[i] = (int)(ranges[key1].range + ranges[key2].range) / 2;
                        }
                        else {
                            values[i] = -9999;
                        }
                    }
                }
                if (computed_type == "mean") {
                    var sum = 0;
                    for (int j = 0; j < range_indices.Count; j++) {
                        var key = range_indices[j];
                        if (ranges.Count > key) {
                            sum += (int)ranges[key].range;
                        }
                        else {
                            sum = -9999;
                            break;
                        }
                    }
                    if (sum == -9999) {
                        values[i] = -9999;
                    }
                    else {
                        values[i] = sum / range_indices.Count;
                    }
                }
            }

            return values;
        }

        public static (int[], int, int, int, int, int) buildStatisticsResponse(int[] computed_values, List<int> indizes, Dictionary<int, int> reverse_indizes, int max_range)
        {
            var counts = new int[max_range / 60 + 1];
            var min = int.MaxValue;
            var max = int.MinValue;
            var mean = 0;
            var values = new List<int>(indizes.Count);
            for (int i = 0; i < indizes.Count; i++) {
                var index = indizes[i];
                var j = reverse_indizes[index];
                var value = computed_values[j];
                if (value == -9999) {
                    continue;
                }
                if (value < min) min = value;
                if (value > max) max = value;
                values.Add(value);
                mean += value;
                counts[(int)(value / 60)] += 1;
            }
            mean = mean / values.Count;
            values.Sort((a, b) => a - b);
            int median;
            if (values.Count % 2 == 1) {
                median = values[(values.Count - 1) / 2];
            }
            else {
                median = (values[(values.Count - 2) / 2] + values[(values.Count - 2) / 2 + 1]) / 2;
            }
            int std = 0;
            return (counts, mean, std, median, min, max);
        }
    }
}