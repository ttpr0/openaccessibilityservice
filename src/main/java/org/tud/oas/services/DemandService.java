package org.tud.oas.services;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;
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
        try {
            if (param.demand_locations != null && param.demand_weights != null) {
                if (param.loc_crs != null) {
                    return this.createDemandView(param.demand_locations, param.demand_weights, param.loc_crs);
                } else {
                    return this.createDemandView(param.demand_locations, param.demand_weights);
                }
            } else if (param.demand_locations != null) {
                if (param.loc_crs != null) {
                    return this.createDemandView(param.demand_locations, param.loc_crs);
                } else {
                    return this.createDemandView(param.demand_locations);
                }
            } else {
                return null;
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

    public IDemandView createDemandView(double[][] locations, double[] weights, String crs) {
        if (crs.equals("EPSG:4326")) {
            return this.createDemandView(locations, weights);
        }
        CoordinateTransform projection = this.createProjection(crs);

        // preallocate coordinates to save unneccessary allocations
        ProjCoordinate in = new ProjCoordinate();
        ProjCoordinate out = new ProjCoordinate();

        List<Coordinate> points = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (int i = 0; i < locations.length; i++) {
            double[] location = locations[i];
            in.x = location[0];
            in.y = location[1];
            projection.transform(in, out);
            points.add(new Coordinate(out.x, out.y));
            counts.add((int) (weights[i]));
        }
        return new DemandView(points, counts);
    }

    public IDemandView createDemandView(double[][] locations, String crs) {
        if (crs.equals("EPSG:4326")) {
            return this.createDemandView(locations);
        }
        CoordinateTransform projection = this.createProjection(crs);

        // preallocate coordinates to save unneccessary allocations
        ProjCoordinate in = new ProjCoordinate();
        ProjCoordinate out = new ProjCoordinate();

        List<Coordinate> points = new ArrayList<>();
        for (int i = 0; i < locations.length; i++) {
            double[] location = locations[i];
            in.x = location[0];
            in.y = location[1];
            projection.transform(in, out);
            points.add(new Coordinate(out.x, out.y));
        }
        return new DemandView(points, null);
    }

    private CoordinateTransform createProjection(String crs) {
        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem geographic = crsFactory.createFromName("EPSG:4326");
        CoordinateReferenceSystem other = crsFactory.createFromName(crs);
        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        return ctFactory.createTransform(other, geographic);
    }
}
