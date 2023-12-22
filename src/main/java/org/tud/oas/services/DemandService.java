package org.tud.oas.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tud.oas.config.OASProperties;
import org.tud.oas.demand.DemandView;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.requests.DemandRequestParams;
import org.tud.oas.util.Pair;

@Service
public class DemandService {

    @Autowired
    public DemandService(OASProperties props) {
    }

    public IDemandView getDemandView(DemandRequestParams param) {
        IDemandView view;
        try {
            if (param.demand_locations != null && param.demand_weights != null) {
                Envelope envelope;
                if (param.envelop != null) {
                    envelope = new Envelope(param.envelop[0], param.envelop[2], param.envelop[1], param.envelop[3]);
                } else {
                    envelope = null;
                }
                view = this.createDemandView(param.demand_locations, param.demand_weights,
                        envelope);
                if (view != null) {
                    return view;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public IDemandView createDemandView(double[][] locations, double[] weights, Envelope envelop) {
        List<Coordinate> points = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (int i = 0; i < locations.length; i++) {
            double[] location = locations[i];
            points.add(new Coordinate(location[0], location[1]));
            counts.add((int) (weights[i]));
        }
        return new DemandView(points, counts);
    }

    public IDemandView createDemandView(double[][] locations, Envelope envelop) {
        List<Coordinate> points = new ArrayList<>();
        for (int i = 0; i < locations.length; i++) {
            double[] location = locations[i];
            points.add(new Coordinate(location[0], location[1]));
        }
        return new DemandView(points, null);
    }
}
