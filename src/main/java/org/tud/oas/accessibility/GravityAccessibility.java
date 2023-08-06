package org.tud.oas.accessibility;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.supply.ISupplyView;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.routing.INNTable;

public class GravityAccessibility {

    public Access[] calcAccessibility(IDemandView demand, ISupplyView supply, List<Double> ranges,
            List<Double> factors, IRoutingProvider provider) {
        Access[] accessibilities = new Access[demand.pointCount()];

        INNTable table = provider.requestNearest(demand, supply, ranges, "isochrones");
        if (table == null) {
            return accessibilities;
        }

        float max_value = 0;
        float max_population = 100;
        for (int p = 0; p < demand.pointCount(); p++) {
            double range = table.getNearestRange(p);
            double factor = factors.get(ranges.indexOf(range));

            Access access;
            if (accessibilities[p] == null) {
                access = new Access();
                accessibilities[p] = access;
            } else {
                access = accessibilities[p];
            }
            access.access += (float) factor;
            if (access.access > max_value) {
                max_value = access.access;
            }
        }

        for (int key = 0; key < accessibilities.length; key++) {
            Access access = accessibilities[key];
            if (access.access == 0) {
                access.access = -9999;
                access.weighted_access = -9999;
            } else {
                access.access = access.access * 100 / max_value;
                access.weighted_access = access.access * demand.getDemand(key) / max_population;
            }
        }
        return accessibilities;
    }

    // public async Task calcAccessibility2(double[][] facilities, List<double>
    // ranges, List<double> factors)
    // {
    // var accessibilities = new Access[this.population.pointCount()];

    // ISourceBlock<IsoRaster?> collection =
    // provider.requestIsoRasterStream(facilities, ranges[ranges.Count - 1]);

    // float max_value = 0;
    // for (int j = 0; j < facilities.Length; j++) {
    // var raster = await collection.ReceiveAsync();
    // if (raster == null) {
    // continue;
    // }
    // double[][] extend = raster.getEnvelope();
    // Envelope env = new Envelope(extend[0][0], extend[3][0], extend[2][1],
    // extend[1][1]);
    // List<int> points = population.getPointsInEnvelop(env);

    // long start = Environment.TickCount64;
    // foreach (int index in points) {
    // Coordinate p = population.getCoordinate(index, "EPSG:25832");
    // var range_dict = raster.getValueAtCoordinate(p);
    // if (range_dict == null) {
    // continue;
    // }
    // bool found = range_dict.TryGetValue(0, out int range);
    // if (found) {
    // Access access;
    // if (accessibilities[index] == null) {
    // access = new Access();
    // accessibilities[index] = access;
    // }
    // else {
    // access = accessibilities[index];
    // }
    // for (int i = 0; i < ranges.Count; i++) {
    // if (range <= ranges[i]) {
    // access.access += (float)factors[i];
    // if (access.access > max_value) {
    // max_value = access.access;
    // }
    // break;
    // }
    // }
    // }
    // }
    // long end = Environment.TickCount64;
    // Console.WriteLine("time: " + (end - start));
    // }

    // for (int index = 0; index < accessibilities.Length; index++) {
    // Access access = accessibilities[index];
    // if (access.access == 0) {
    // access.access = -9999;
    // access.weighted_access = -9999;
    // }
    // else {
    // access.access = access.access * 100 / max_value;
    // access.weighted_access = access.access * this.population.getPopulation(index)
    // / max_population;
    // }
    // }
    // this.accessibility = accessibilities;
    // }
}
