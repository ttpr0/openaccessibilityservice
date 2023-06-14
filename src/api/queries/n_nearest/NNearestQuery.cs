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
        public static float[] computeQuery(double[] values, IKNNTable table, string computed_type, int count)
        {
            var results = new float[values.Length];
            for (int i = 0; i < values.Length; i++) {
                int index = i;
                List<double> vals = Enumerable.Range(0, count).Select(item => {
                    var (key, _) = table.getKNearest(index, item);
                    if (key == -1) {
                        return -9999;
                    }
                    return values[key];
                }).Where(item => item != -9999).ToList();
                if (computed_type == "min") {
                    if (vals.Count == 0) {
                        results[i] = -9999;
                        continue;
                    }
                    results[i] = (float)vals.Min();
                }
                if (computed_type == "max") {
                    if (vals.Count < count) {
                        results[i] = -9999;
                        continue;
                    }
                    results[i] = (float)vals.Max();
                }
                if (computed_type == "median") {
                    if (vals.Count < count) {
                        results[i] = -9999;
                        continue;
                    }
                    vals.Sort();
                    if (vals.Count % 2 == 1) {
                        var key = (vals.Count - 1) / 2;
                        results[i] = (float)vals[key];
                    }
                    else {
                        var key1 = (vals.Count - 2) / 2;
                        var key2 = (vals.Count - 2) / 2 + 1;
                        results[i] = (float)(vals[key1] + vals[key2]) / 2;
                    }
                }
                if (computed_type == "mean") {
                    if (vals.Count < count) {
                        results[i] = -9999;
                        continue;
                    }
                    else {
                        results[i] = (float)(vals.Sum() / vals.Count);
                    }
                }
                if (computed_type == "sum") {
                    if (vals.Count < count) {
                        results[i] = -9999;
                        continue;
                    }
                    else {
                        results[i] = (float)vals.Sum();
                    }
                }
            }

            return results;
        }
    }
}