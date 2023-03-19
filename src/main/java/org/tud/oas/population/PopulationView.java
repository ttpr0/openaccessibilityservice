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
    String population_type;
    int[] population_indizes;

    public PopulationView(Population population, Envelope envelope) {
        this.population = population;
        this.envelope = envelope;
        this.area = null;
        this.population_type = "standard_all";
    }

    public PopulationView(Population population, Envelope envelope, String population_type, int[] population_indizes) {
        this.population = population;
        this.envelope = envelope;
        this.area = null;
        this.population_type = population_type;
        this.population_indizes = population_indizes;
    }

    public PopulationView(Population population, Geometry area) {
        this.population = population;
        this.area = area;
        this.envelope = area.getEnvelopeInternal();
    }

    public Envelope getEnvelope() {
        return this.envelope;
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

    public int getPopulationCount(int index) {
        PopulationAttributes attrs = this.population.attributes.get(index);
        if (this.population_type == null || this.population_type.equals("standard_all")) {
            return attrs.getPopulationCount();
        } 
        if (this.population_type.equals("standard")) {
            return attrs.getStandardPopulation(population_indizes);
        }
        if (this.population_type.equals("kita_schul")) {
            return attrs.getKitaSchulPopulation(population_indizes);
        }
        return 0;
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

    public String getPopulationType() {
        return population_type;
    }

    public void setPopulationType(String population_type) {
        this.population_type = population_type;
    }

    public int[] getPopulationIndizes() {
        return population_indizes;
    }

    public void setPopulationIndizes(int[] population_indizes) {
        this.population_indizes = population_indizes;
    }
}
