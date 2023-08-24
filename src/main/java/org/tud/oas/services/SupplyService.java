package org.tud.oas.services;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.springframework.stereotype.Service;
import org.tud.oas.requests.SupplyRequestParams;
import org.tud.oas.supply.ISupplyView;
import org.tud.oas.supply.SupplyView;

@Service
public class SupplyService {
    public ISupplyView getSupplyView(SupplyRequestParams param) {
        ISupplyView view;
        try {
            if (param.supply_locations != null && param.supply_weights != null) {
                view = this.createSupplyView(param.supply_locations, param.supply_weights);
                if (view != null) {
                    return view;
                }
            } else if (param.supply_locations != null) {
                view = this.createSupplyView(param.supply_locations);
                if (view != null) {
                    return view;
                }
            } else if (param.supply_weights != null) {
                view = this.createSupplyView(param.supply_weights);
                if (view != null) {
                    return view;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ISupplyView createSupplyView(double[][] locations, double[] weights) {
        List<Coordinate> points = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (int i = 0; i < locations.length; i++) {
            double[] location = locations[i];
            points.add(new Coordinate(location[0], location[1]));
            counts.add((int) (weights[i]));
        }
        return new SupplyView(points, counts);
    }

    public ISupplyView createSupplyView(double[][] locations) {
        List<Coordinate> points = new ArrayList<>();
        for (int i = 0; i < locations.length; i++) {
            double[] location = locations[i];
            points.add(new Coordinate(location[0], location[1]));
        }
        return new SupplyView(points, null);
    }

    public ISupplyView createSupplyView(double[] weights) {
        List<Integer> counts = new ArrayList<>();
        for (int i = 0; i < weights.length; i++) {
            counts.add((int) (weights[i]));
        }
        return new SupplyView(null, counts);
    }
}
