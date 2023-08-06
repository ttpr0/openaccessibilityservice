package org.tud.oas.supply;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

public interface ISupplyView {
    public Coordinate getCoordinate(int index);

    public int getSupply(int index);

    public int pointCount();

    public Iterable<Integer> getPoints();

    public Iterable<Integer> getPointsInEnvelop(Envelope envelope);

    public Iterable<Integer> getPointsInArea(Geometry area);
}