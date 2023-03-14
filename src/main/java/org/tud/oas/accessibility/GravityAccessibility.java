package org.tud.oas.accessibility;

import org.tud.oas.api.accessibility.GridFeature;
import org.tud.oas.api.accessibility.GridResponse;
import org.tud.oas.population.Population;
import org.tud.oas.population.PopulationAttributes;
import org.tud.oas.routing.IRoutingProvider;
import org.tud.oas.routing.IsoRaster;
import org.tud.oas.routing.IsochroneCollection;
import org.tud.oas.routing.Isochrone;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.simplify.PolygonHullSimplifier;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.algorithm.locate.SimplePointInAreaLocator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

class Access {
    public float access;
    public float weighted_access;
}

public class GravityAccessibility {
    private Population population;
    private IRoutingProvider provider;

    private float max_population;
    private Map<Integer, Access> accessibility;

    public GravityAccessibility(Population population, IRoutingProvider provider) {
        this.population = population;
        this.provider = provider;

        this.max_population = 100;
        this.accessibility = new HashMap<Integer, Access>();
    }

    public Map<Integer, Access> getAccessibility() {
        return this.accessibility;
    }

    public void calcAccessibility(Double[][] facilities, List<Double> ranges, List<Double> factors) throws Exception {
        Set<Boolean> visited = new HashSet<Boolean>(10000);
        Map<Integer, Access> accessibilities = new HashMap<Integer, Access>(10000);

        HashMap<Double, Geometry> polygons = new HashMap<Double, Geometry>(ranges.size());

        // Double[][] locations = new Double[1][2];
        // for (int f=0; f<facilities.length; f++) {
        //     locations[0][0] = facilities[f][0];
        //     locations[0][1] = facilities[f][1];
        //     List<IsochroneCollection> isochrones_coll = provider.requestIsochrones(locations, ranges);
        //     if (isochrones_coll == null) {
        //         continue;
        //     }
        //     IsochroneCollection isochrones = isochrones_coll.get(0);

        BlockingQueue<IsochroneCollection> collection = provider.requestIsochronesStream(facilities, ranges);
        for (int f=0; f<facilities.length; f++) {
            IsochroneCollection isochrones = collection.take();
            if (isochrones.getIsochrones() == null) {
                continue;
            }

            for (int i=0; i< isochrones.getIsochronesCount(); i++) {
                Isochrone isochrone = isochrones.getIsochrone(i);
                double range = isochrone.getValue();
                
                if (!polygons.containsKey(range)) {
                    polygons.put(range, isochrone.getGeometry());
                }
                else {
                    Geometry geometry = polygons.get(range);
                    polygons.put(range, geometry.union(isochrone.getGeometry()));
                }
            }
        }

        float max_value = 0;
        for (int i=0; i<ranges.size(); i++) {
            double range = ranges.get(i);
            double factor = factors.get(i);
            Geometry iso = polygons.get(range);

            Envelope env = iso.getEnvelopeInternal();
            List<Integer> points = population.getPointsInEnvelop(env);

            Geometry geom = new PolygonHullSimplifier(iso, false).getResult();

            long start = System.currentTimeMillis();
            for (Integer index : points) {
                PopulationAttributes attr = population.getAttributes(index);
                if (visited.contains(index)) {
                    continue;
                }
                Coordinate p = population.getPoint(index);
                int location = SimplePointInAreaLocator.locate(p, geom);
                if (location == Location.INTERIOR) {
                // if (p.getPoint().within(geom)) {
                    Access access;
                    if (!accessibilities.containsKey(index)) {
                        access = new Access();
                        accessibilities.put(index, access);
                    } else {
                        access = accessibilities.get(index);
                    }
                    accessibilities.get(index).access += factor;
                    if (access.access > max_value) {
                        max_value = access.access;
                    }
                }
            }
            long end = System.currentTimeMillis();
            System.out.println("Time: " + (end - start));
        }

        for (Integer key : accessibilities.keySet()) {
            Access access = accessibilities.get(key);
            if (access.access == 0) {
                access.access = -9999;
                access.weighted_access = -9999;
            }
            else {
                access.access = access.access * 100 / max_value;
                access.weighted_access = access.access * this.population.attributes.get(key).getPopulationCount() / max_population;
            }
        }
        this.accessibility = accessibilities;
    }

    public void calcAccessibility2(Double[][] facilities, List<Double> ranges, List<Double> factors) throws Exception {
        Map<Integer, Access> accessibilities = new HashMap<Integer, Access>(10000);

        BlockingQueue<IsoRaster> collection = provider.requestIsoRasterStream(facilities, ranges.get(ranges.size()-1));

        float max_value = 0;
        for (int f=0; f<facilities.length; f++) {
            IsoRaster raster = collection.take();
            if (raster.getEnvelope() == null) {
                continue;
            }
            double[][] extend = raster.getEnvelope();
            Envelope env = new Envelope(extend[0][0], extend[3][0], extend[2][1], extend[1][1]);
            List<Integer> points = population.getPointsInEnvelop(env);

            long start = System.currentTimeMillis();
            for (Integer index : points) {
                PopulationAttributes attr = population.getAttributes(index);
                Coordinate p = population.getUTMPoint(index);
                int range = raster.getValueAtCoordinate(p);
                if (range != -1) {
                    Access access;
                    if (!accessibilities.containsKey(index)) {
                        access = new Access();
                        accessibilities.put(index, access);
                    } else {
                        access = accessibilities.get(index);
                    }
                    for (int i=0; i<ranges.size(); i++) {
                        if (range <= ranges.get(i)) {
                            access.access += factors.get(i);
                            if (access.access > max_value) {
                                max_value = access.access;
                            }
                            break;
                        }
                    }
                }
            }
            long end = System.currentTimeMillis();
            System.out.println("time: " + (end - start));
        }

        for (Integer index : accessibilities.keySet()) {
            Access access = accessibilities.get(index);
            if (access.access == 0) {
                access.access = -9999;
                access.weighted_access = -9999;
            }
            else {
                access.access = access.access * 100 / max_value;
                access.weighted_access = access.access * this.population.attributes.get(index).getPopulationCount() / max_population;
            }
        }
        this.accessibility = accessibilities;
    }

    public GridResponse buildResponse() {
        List<GridFeature> features = new ArrayList<GridFeature>();
        float minx = 1000000000;
        float maxx = -1;
        float miny = 1000000000;
        float maxy = -1;
        for (int i=0; i< population.getPointCount(); i++) {
            Coordinate p = population.getUTMPoint(i);
            if (this.accessibility.containsKey(i)) {
                Access access = this.accessibility.get(i);
                GravityValue value = new GravityValue(access.access, access.weighted_access);
                features.add(new GridFeature((float)p.getX(), (float)p.getY(), value));
            } else {
                GravityValue value = new GravityValue(-9999, -9999);
                features.add(new GridFeature((float)p.getX(), (float)p.getY(), value));
            }
            if (p.getX() < minx) {
                minx = (float)p.getX();
            }
            if (p.getX() > maxx) {
                maxx = (float)p.getX();
            }
            if (p.getY() < miny) {
                miny = (float)p.getY();
            }
            if (p.getY() > maxy) {
                maxy = (float)p.getY();
            }
        }
        float[] extend = {minx-50, miny-50, maxx+50, maxy+50};

        float dx = extend[2] - extend[0];
        float dy = extend[3] - extend[1];
        int[] size = {(int)(dx/100), (int)(dy/100)};

        String crs = "EPSG:25832";

        return new GridResponse(features, crs, extend, size);
    }
}

class GravityValue {
    public float unweighted;
    public float weighted;

    public GravityValue(float unweighted, float weighted) {
        this.unweighted = unweighted;
        this.weighted = weighted;
    }
}