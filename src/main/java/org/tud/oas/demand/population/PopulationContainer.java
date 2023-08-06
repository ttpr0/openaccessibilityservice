package org.tud.oas.demand.population;

import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.algorithm.locate.SimplePointInAreaLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.index.kdtree.*;

public class PopulationContainer {
    private KdTree index;
    private List<Coordinate> points;
    private List<Coordinate> utm_points;
    private List<PopulationAttributes> attributes;

    public PopulationContainer(int initial_size) {
        this.index = new KdTree();
        this.points = new ArrayList<>(initial_size);
        this.utm_points = new ArrayList<>(initial_size);
        this.attributes = new ArrayList<>(initial_size);
    }

    public void addPopulationPoint(Coordinate point, Coordinate utm_point, PopulationAttributes attributes) {
        int index = this.points.size();
        attributes.setIndex(index);
        this.points.add(point);
        this.utm_points.add(utm_point);
        this.attributes.add(attributes);
        this.index.insert(point, index);
    }

    public Coordinate getPoint(int index) {
        return this.points.get(index);
    }

    public Coordinate getUTMPoint(int index) {
        return this.utm_points.get(index);
    }

    public PopulationAttributes getAttributes(int index) {
        return this.attributes.get(index);
    }

    public int getPointCount() {
        return this.points.size();
    }

    public KdTree getIndex() {
        return this.index;
    }

    public List<Integer> getPointsInEnvelop(Envelope envelope) {
        List<Integer> points = new ArrayList<>(100);

        this.index.query(envelope, node -> {
            int index = (int) node.getData();
            points.add(index);
        });

        return points;
    }

    public List<Integer> getPointsInGeometry(Geometry area) {
        List<Integer> points = new ArrayList<>(100);

        Envelope envelope = area.getEnvelopeInternal();

        this.index.query(envelope, node -> {
            int location = SimplePointInAreaLocator.locate(node.getCoordinate(), area);
            if (location == Location.INTERIOR) {
                int index = (int) node.getData();
                points.add(index);
            }
        });

        return points;
    }
}
