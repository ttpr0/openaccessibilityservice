package org.tud.oas.services;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tud.oas.config.OASProperties;
import org.tud.oas.demand.DemandView;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.requests.DemandRequestParams;

@Service
public class DemandService {

    @Autowired
    public DemandService(OASProperties props) {
    }

    public IDemandView getDemandView(DemandRequestParams param) {
        IDemandView view;
        try {
            if (param.demand_locations != null && param.demand_weights != null) {
                view = this.createDemandView(param.demand_locations, param.demand_weights);
                if (view != null) {
                    return view;
                }
            } else if (param.demand_locations != null) {
                view = this.createDemandView(param.demand_locations);
                if (view != null) {
                    return view;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public IDemandView createDemandView(double[][] locations, double[] weights) {
        List<Coordinate> points = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (int i = 0; i < locations.length; i++) {
            double[] location = locations[i];
            points.add(new Coordinate(location[0], location[1]));
            counts.add((int) (weights[i]));
        }
        return new DemandView(points, counts);
    }

    public IDemandView createDemandView(double[][] locations) {
        List<Coordinate> points = new ArrayList<>();
        for (int i = 0; i < locations.length; i++) {
            double[] location = locations[i];
            points.add(new Coordinate(location[0], location[1]));
        }
        return new DemandView(points, null);
    }
}
