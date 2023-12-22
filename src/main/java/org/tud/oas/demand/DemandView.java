package org.tud.oas.demand;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.kdtree.*;
import org.locationtech.jts.algorithm.locate.*;

import java.util.ArrayList;
import java.util.List;

public class DemandView implements IDemandView {
    private KdTree index;
    private List<Coordinate> points;
    private List<Integer> counts;

    public DemandView(List<Coordinate> points, List<Integer> counts) {
        this.points = points;
        this.counts = counts;
        this.index = new KdTree();
        int i = 0;
        for (Coordinate coord : points) {
            this.index.insert(coord, i);
            i++;
        }
    }

    public Coordinate getCoordinate(int index) {
        return this.points.get(index);
    }

    public int getDemand(int index) {
        if (this.counts == null) {
            return 1;
        }
        return this.counts.get(index);
    }

    public int pointCount() {
        return this.points.size();
    }

    public Iterable<Integer> getPointsInEnvelop(Envelope envelope) {
        Envelope env = envelope;
        if (env == null) {
            List<Integer> result = new ArrayList<>();
            for (int i = 0; i < pointCount(); i++) {
                result.add(i);
            }
            return result;
        }

        List<Integer> points = new ArrayList<>(100);

        this.index.query(env, node -> {
            int index = (int) node.getData();
            points.add(index);
        });

        return points;
    }

    public Iterable<Integer> getPointsInArea(Geometry area) {
        Envelope env = area.getEnvelopeInternal();
        if (env == null) {
            List<Integer> result = new ArrayList<>();
            for (int i = 0; i < pointCount(); i++) {
                result.add(i);
            }
            return result;
        }

        List<Integer> points = new ArrayList<>(100);

        this.index.query(env, node -> {
            int location = SimplePointInAreaLocator.locate(node.getCoordinate(), area);
            if (location == Location.INTERIOR) {
                int index = (int) node.getData();
                points.add(index);
            }
        });

        return points;
    }
}
