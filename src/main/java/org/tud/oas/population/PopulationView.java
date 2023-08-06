package org.tud.oas.population;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.kdtree.*;
import org.locationtech.jts.algorithm.locate.*;

import java.util.ArrayList;
import java.util.List;

public class PopulationView implements IPopulationView {
    private KdTree index;
    private List<Coordinate> points;
    private List<Coordinate> utm_points;
    private List<Integer> counts;

    private Geometry area;
    private Envelope envelope;

    public PopulationView(List<Coordinate> points, List<Coordinate> utm_points, List<Integer> counts,
            Envelope envelope) {
        this.points = points;
        this.utm_points = utm_points;
        this.counts = counts;
        this.index = new KdTree();
        int i = 0;
        for (Coordinate coord : points) {
            this.index.insert(coord, i);
            i++;
        }
        this.envelope = envelope;
        this.area = null;
    }

    public PopulationView(List<Coordinate> points, List<Coordinate> utm_points, List<Integer> counts, Geometry area) {
        this.points = points;
        this.utm_points = utm_points;
        this.counts = counts;
        this.index = new KdTree();
        int i = 0;
        for (Coordinate coord : points) {
            this.index.insert(coord, i);
            i++;
        }
        if (area != null) {
            this.area = area;
            this.envelope = area.getEnvelopeInternal();
        }
    }

    public Coordinate getCoordinate(int index) {
        return this.points.get(index);
    }

    public Coordinate getCoordinate(int index, String crs) {
        if ("EPSG:4326".equals(crs)) {
            return this.points.get(index);
        } else if ("EPSG:25832".equals(crs)) {
            return this.utm_points.get(index);
        }
        return new Coordinate(0, 0);
    }

    public int getPopulation(int index) {
        return this.counts.get(index);
    }

    public int pointCount() {
        return this.points.size();
    }

    public List<Integer> getPointsInEnvelop(Envelope envelope) {
        Envelope env;
        if (this.envelope == null) {
            env = envelope;
        } else {
            env = this.envelope.intersection(envelope);
        }
        if (env == null) {
            List<Integer> result = new ArrayList<>();
            for (int i = 0; i < pointCount(); i++) {
                result.add(i);
            }
            return result;
        }

        List<Integer> points = new ArrayList<>(100);

        this.index.query(env, node -> {
            if (this.area == null) {
                int index = (int) node.getData();
                points.add(index);
            } else {
                int location = SimplePointInAreaLocator.locate(node.getCoordinate(), this.area);
                if (location == Location.INTERIOR) {
                    int index = (int) node.getData();
                    points.add(index);
                }
            }
        });

        return points;
    }
}
