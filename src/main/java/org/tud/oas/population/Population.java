package org.tud.oas.population;

import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdTree;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;

import java.util.List;
import java.util.ArrayList;

public class Population {
    public KdTree index;

    public List<PopulationPoint> p_points;

    public List<Point> points;

    public List<PopulationAttributes> attributes;

    public Population(int initial_size) {
        this.index = new KdTree();
        this.points = new ArrayList<Point>(initial_size);
        this.attributes = new ArrayList<PopulationAttributes>(initial_size);
        this.p_points = new ArrayList<PopulationPoint>(initial_size);
    }

    public void addPopulationPoint(Point point, float x, float y, PopulationAttributes attributes) {
        int index = this.points.size();
        attributes.setIndex(index);
        this.points.add(point);
        this.attributes.add(attributes);
        this.p_points.add(new PopulationPoint(point, x, y, attributes));
        this.index.insert(point.getCoordinate(), attributes);
    }

    public PopulationPoint getPoint(int index) {
        return this.p_points.get(index);
    }

    public int getPointCount() {
        return this.points.size();
    }

    public List<PopulationPoint> getPointsInEnvelop(Envelope envelope) {
        List<PopulationPoint> points = new ArrayList<PopulationPoint>(100);

        this.index.query(envelope, (KdNode node) -> {
            PopulationAttributes data = (PopulationAttributes)node.getData();
            int index = data.getIndex();
            points.add(this.p_points.get(index));
        });

        return points;
    }
}
