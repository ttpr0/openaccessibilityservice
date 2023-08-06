package org.tud.oas.population;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.locate.SimplePointInAreaLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdTree;

public class PopulationContainerView2 {
    private PopulationContainer population;
    private Geometry area;
    private Envelope envelope;
    private String population_type;
    private int[] population_indizes;

    public PopulationContainerView2(PopulationContainer population, Envelope envelope) {
        this.population = population;
        this.envelope = envelope;
        this.area = null;
        this.population_type = "standard_all";
    }

    public PopulationContainerView2(PopulationContainer population, Envelope envelope, String population_type,
            int[] population_indizes) {
        this.population = population;
        this.envelope = envelope;
        this.area = null;
        if (population_indizes == null && !population_type.equals("standard_all")) {
            throw new IllegalArgumentException("invalid arguments passed to constructor");
        }
        this.population_type = population_type;
        this.population_indizes = population_indizes;
    }

    public PopulationContainerView2(PopulationContainer population, Geometry area) {
        this.population = population;
        if (area != null) {
            this.area = area;
            this.envelope = area.getEnvelopeInternal();
        }
        this.population_type = "standard_all";
    }

    public Envelope getEnvelope() {
        return this.envelope;
    }

    public Coordinate getCoordinate(int index) {
        return this.population.getPoint(index);
    }

    public Coordinate getCoordinate(int index, String crs) {
        if (crs.equals("EPSG:4326")) {
            return this.population.getPoint(index);
        } else if (crs.equals("EPSG:25832")) {
            return this.population.getUTMPoint(index);
        }
        return new Coordinate(0, 0);
    }

    public int getPopulation(int index) {
        PopulationAttributes attrs = this.population.getAttributes(index);
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
        List<Integer> points = new ArrayList<>();

        if (this.envelope == null) {
            for (int i = 0; i < this.population.getPointCount(); i++) {
                points.add(i);
            }
            return points;
        }

        KdTree index = this.population.getIndex();

        index.query(this.envelope, node -> {
            int indexValue = (int) node.getData();
            points.add(indexValue);
        });

        return points;
    }

    public List<Integer> getPointsInEnvelop(Envelope envelope) {
        Envelope env;
        if (this.envelope == null) {
            env = envelope;
        } else {
            env = this.envelope.intersection(envelope);
        }
        if (env == null) {
            return this.getAllPoints();
        }

        List<Integer> points = new ArrayList<>();
        KdTree index = this.population.getIndex();

        index.query(env, node -> {
            if (this.area == null) {
                int indexValue = (int) node.getData();
                points.add(indexValue);
            } else {
                int location = SimplePointInAreaLocator.locate(node.getCoordinate(), this.area);
                if (location == Location.INTERIOR) {
                    int indexValue = (int) node.getData();
                    points.add(indexValue);
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
