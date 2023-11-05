package org.tud.oas.demand.population;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.locationtech.jts.algorithm.locate.SimplePointInAreaLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.index.kdtree.*;

public class PopulationContainer {
    private KdTree index;
    private HashMap<Integer, String> population_keys;
    private List<PopulationAttributes> attributes;

    public PopulationContainer(int initial_size, HashMap<Integer, String> keys) {
        this.index = new KdTree();
        this.population_keys = keys;
        this.attributes = new ArrayList<>(initial_size);
    }

    public void addPopulationPoint(Coordinate point, Coordinate utm_point, int[] values) {
        int index = this.attributes.size();
        PopulationAttributes attr = new PopulationAttributes(point, utm_point, values);
        this.attributes.add(attr);
        this.index.insert(point, index);
    }

    public Coordinate getPoint(int index) {
        return this.attributes.get(index).getPoint();
    }

    public Coordinate getUTMPoint(int index) {
        return this.attributes.get(index).getUTMPoint();
    }

    public PopulationAttributes getAttributes(int index) {
        return this.attributes.get(index);
    }

    public int getPointCount() {
        return this.attributes.size();
    }

    public KdTree getIndex() {
        return this.index;
    }

    /**
     * Returns the number of population values per location.
     */
    public int getValueCount() {
        return this.population_keys.size();
    }

    /**
     * Returns the Key of a given population value (at index).
     */
    public String getValueKey(int index) {
        return this.population_keys.get(index);
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
