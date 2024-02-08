package org.tud.oas.services;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;
import org.springframework.stereotype.Service;
import org.tud.oas.requests.SupplyRequestParams;
import org.tud.oas.supply.ISupplyView;
import org.tud.oas.supply.SupplyView;

@Service
public class SupplyService {
    public ISupplyView getSupplyView(SupplyRequestParams param) {
        try {
            if (param.supply_locations != null && param.supply_weights != null) {
                if (param.loc_crs != null) {
                    return this.createSupplyView(param.supply_locations, param.supply_weights, param.loc_crs);
                } else {
                    return this.createSupplyView(param.supply_locations, param.supply_weights);
                }
            } else if (param.supply_locations != null) {
                if (param.loc_crs != null) {
                    return this.createSupplyView(param.supply_locations, param.loc_crs);
                } else {
                    return this.createSupplyView(param.supply_locations);
                }
            }
            return null;
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

    public ISupplyView createSupplyView(double[][] locations, double[] weights, String crs) {
        if (crs.equals("EPSG:4326")) {
            return this.createSupplyView(locations);
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
        return new SupplyView(points, counts);
    }

    public ISupplyView createSupplyView(double[][] locations, String crs) {
        if (crs.equals("EPSG:4326")) {
            return this.createSupplyView(locations);
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
        return new SupplyView(points, null);
    }

    private CoordinateTransform createProjection(String crs) {
        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem geographic = crsFactory.createFromName("EPSG:4326");
        CoordinateReferenceSystem other = crsFactory.createFromName(crs);
        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        return ctFactory.createTransform(other, geographic);
    }
}
