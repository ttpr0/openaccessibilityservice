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
    public class AggregateQuery
    {
        public static async Task<List<int>[]> computeAccessibility(double[][] locations, double range, IPopulationView view, IRoutingProvider provider)
        {
            var simple = new SimpleCatchment(view, provider);

            await simple.calcAccessibility(locations, range);

            return simple.getAccessibilities();
        }

        public static float[] computeQuery(double[] values, List<int>[] accessibilities, string computed_type)
        {
            var results = new float[accessibilities.Length];
            for (int i = 0; i < accessibilities.Length; i++) {
                int index = i;
                List<int> facilities;
                if (accessibilities[index] != null) {
                    facilities = accessibilities[index];
                }
                else {
                    facilities = new List<int>();
                }
                if (computed_type == "min") {
                    if (facilities.Count == 0) {
                        results[i] = -9999;
                    }
                    else {
                        results[i] = (float)facilities.Min(item => values[item]);
                    }
                }
                if (computed_type == "max") {
                    if (facilities.Count == 0) {
                        results[i] = -9999;
                    }
                    else {
                        results[i] = (float)facilities.Max(item => values[item]);
                    }
                }
                if (computed_type == "median") {
                    if (facilities.Count == 0) {
                        results[i] = -9999;
                    }
                    else {
                        var temp = facilities.Select(item => values[item]).ToList();
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
                    if (facilities.Count == 0) {
                        results[i] = -9999;
                    }
                    else {
                        float sum = 0;
                        for (int j = 0; j < facilities.Count; j++) {
                            var key = facilities[j];
                            sum += (float)values[key];
                        }
                        results[i] = sum / facilities.Count;
                    }
                }
                if (computed_type == "sum") {
                    if (facilities.Count == 0) {
                        results[i] = -9999;
                    }
                    else {
                        float sum = 0;
                        for (int j = 0; j < facilities.Count; j++) {
                            var key = facilities[j];
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