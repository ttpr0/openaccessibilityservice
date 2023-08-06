package org.tud.oas.population;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

public interface IPopulationView {
    public Coordinate getCoordinate(int index);

    public Coordinate getCoordinate(int index, String crs);

    public int getPopulation(int index);

    public int pointCount();

    public List<Integer> getPointsInEnvelop(Envelope envelope);
}