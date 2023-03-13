package org.tud.oas.population;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.algorithm.locate.SimplePointInAreaLocator;

public class PopulationView {
    Population population;
    Geometry area;
    Envelope envelope;

    public PopulationView(Population population, Envelope envelope) {
        this.population = population;
        this.envelope = envelope;
        this.area = null;
    }

    public PopulationView(Population population, Geometry area) {
        this.population = population;
        this.area = area;
        this.envelope = area.getEnvelopeInternal();
    }

    public Envelope getEnvelope() {
        return this.envelope;
    }

    public PopulationAttributes getAttributes(int index) {
        return this.population.attributes.get(index);
    }

    public Coordinate getCoordinate(int index) {
        return this.population.points.get(index);
    }

    public Coordinate getCoordinate(int index, String crs) {
        if (crs == "EPSG:4326") {
            return this.population.points.get(index);
        } else if (crs == "EPSG:25832") {
            return this.population.utm_points.get(index);
        }
        return new Coordinate(0, 0);
    }

    public List<Integer> getAllPoints() {
        List<Integer> points = new ArrayList<Integer>(100);

        this.population.index.query(this.envelope, (KdNode node) -> {
            Integer index = (Integer)node.getData();
            points.add(index);
        });

        return points;
    }

    public List<Integer> getPointsInEnvelop(Envelope envelope) {
        List<Integer> points = new ArrayList<Integer>(100);

        Envelope env = this.envelope.intersection(envelope);
        if (env == null) {
            return points;
        }

        this.population.index.query(env, (KdNode node) -> {
            if (this.area == null) {
                Integer index = (Integer)node.getData();
                points.add(index);
            } else {
                int location = SimplePointInAreaLocator.locate(node.getCoordinate(), this.area);
                if (location == Location.INTERIOR) {
                    Integer index = (Integer)node.getData();
                    points.add(index);
                }
            }
        });

        return points;
    }
}
