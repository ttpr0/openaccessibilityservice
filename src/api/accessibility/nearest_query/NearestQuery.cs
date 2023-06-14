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
        public static async Task<List<RangeRef>[]> computeAccessibility(double[][] locations, List<double> ranges, int count, IPopulationView view, IRoutingProvider provider)
        {
            var table = await provider.requestKNearest(view, locations, ranges, count, "isochrones");

            var access = new List<RangeRef>[view.pointCount()];
            for (int p = 0; p < view.pointCount(); p++) {
                access[p] = new List<RangeRef>(count);
                for (int i = 0; i < count; i++) {
                    var (index, range) = table.getKNearest(p, i);
                    access[p].Add(new RangeRef(range, index));
                }
            }

            return access;
        }

        public static Dictionary<string, int>[] buildResponse(IPopulationView population, List<RangeRef>[] accessibilities, int count)
        {
            var features = new Dictionary<string, int>[population.pointCount()];

            for (int i = 0; i < population.pointCount(); i++) {
                int index = i;
                List<RangeRef> ranges;
                if (accessibilities[index] != null) {
                    ranges = accessibilities[index];
                }
                else {
                    ranges = new List<RangeRef>();
                }
                accessibilities[index] = ranges;
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
                features[index] = value;
            }

            return features;
        }

        public static int[] buildComputeResponse(List<RangeRef>[] accessibilities, string computed_type, List<int> range_indices)
        {
            range_indices.Sort((int a, int b) => a - b);
            var values = new int[accessibilities.Length];
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

        public static (int[], int, int, int, int, int) buildStatisticsResponse(int[] computed_values, List<int> indizes, int max_range)
        {
            var counts = new int[max_range / 60 + 1];
            var min = int.MaxValue;
            var max = int.MinValue;
            var mean = 0;
            var values = new List<int>(indizes.Count);
            for (int i = 0; i < indizes.Count; i++) {
                var index = indizes[i];
                var value = computed_values[index];
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