package org.tud.oas.demand;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

public interface IDemandView {
    public Coordinate getCoordinate(int index);

    public int getDemand(int index);

    public int pointCount();

    public Iterable<Integer> getPointsInEnvelop(Envelope envelope);
}