package org.tud.oas.supply;

import org.locationtech.jts.geom.*;
import org.tud.oas.util.Range;
import org.locationtech.jts.algorithm.locate.*;

import java.util.ArrayList;
import java.util.List;

public class SupplyView implements ISupplyView {
    private List<Coordinate> points;
    private List<Integer> counts;

    public SupplyView(List<Coordinate> points, List<Integer> counts) {
        this.points = points;
        this.counts = counts;
    }

    public Coordinate getCoordinate(int index) {
        return this.points.get(index);
    }

    public int getSupply(int index) {
        if (this.counts == null) {
            return 1;
        }
        return this.counts.get(index);
    }

    public int pointCount() {
        return this.points.size();
    }

    public Iterable<Integer> getPoints() {
        return new Range(0, this.pointCount());
    }

    public List<Integer> getPointsInEnvelop(Envelope envelope) {
        Envelope env = envelope;
        if (env == null) {
            List<Integer> result = new ArrayList<>();
            for (int i = 0; i < pointCount(); i++) {
                result.add(i);
            }
            return result;
        } else {
            List<Integer> result = new ArrayList<>();
            for (int i = 0; i < pointCount(); i++) {
                Coordinate p = this.points.get(i);
                if (env.contains(p)) {
                    result.add(i);
                }
            }
            return result;
        }
    }

    public List<Integer> getPointsInArea(Geometry area) {
        Envelope env = area.getEnvelopeInternal();
        if (env == null) {
            List<Integer> result = new ArrayList<>();
            for (int i = 0; i < pointCount(); i++) {
                result.add(i);
            }
            return result;
        } else {
            List<Integer> result = new ArrayList<>();
            for (int i = 0; i < pointCount(); i++) {
                Coordinate p = this.points.get(i);
                int location = SimplePointInAreaLocator.locate(p, area);
                if (location == Location.INTERIOR) {
                    result.add(i);
                }
            }
            return result;
        }
    }
}
