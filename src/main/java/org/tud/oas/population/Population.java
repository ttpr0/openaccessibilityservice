package org.tud.oas.population;

import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdTree;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.util.List;
import java.util.ArrayList;

public class Population {
    public KdTree index;

    public List<Coordinate> points;

    public List<Coordinate> utm_points;

    public List<PopulationAttributes> attributes;

    public Population(int initial_size) {
        this.index = new KdTree();
        this.points = new ArrayList<Coordinate>(initial_size);
        this.utm_points = new ArrayList<Coordinate>(initial_size);
        this.attributes = new ArrayList<PopulationAttributes>(initial_size);
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

    public List<Integer> getPointsInEnvelop(Envelope envelope) {
        List<Integer> points = new ArrayList<Integer>(100);

        this.index.query(envelope, (KdNode node) -> {
            Integer index = (Integer)node.getData();
            points.add(index);
        });

        return points;
    }

    public PopulationView getPopulationView(Envelope envelope) {
        return new PopulationView(this, envelope);
    }

    public PopulationView getPopulationView(Geometry area) {
        return new PopulationView(this, area);
    }
}
