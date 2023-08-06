package org.tud.oas.demand;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

public interface IDemandView {
    public Coordinate getCoordinate(int index);

    public Coordinate getCoordinate(int index, String crs);

    public int getDemand(int index);

    public int pointCount();

    public List<Integer> getPointsInEnvelop(Envelope envelope);
}