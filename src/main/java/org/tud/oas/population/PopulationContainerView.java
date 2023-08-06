package org.tud.oas.population;

import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.kdtree.KdTree;

public class PopulationContainerView implements IPopulationView {
    private KdTree index;
    private List<Integer> points;
    private PopulationContainer population;
    private Geometry area;
    private Envelope envelope;
    private String population_type;
    private int[] population_indizes;

    public PopulationContainerView(PopulationContainer population, Envelope envelope) {
        this.index = new KdTree();
        this.points = new ArrayList<>();
        List<Integer> indizes = population.getPointsInEnvelop(envelope);
        for (Integer index : indizes) {
            this.index.insert(population.getPoint(index), this.points.size());
            this.points.add(index);
        }
        this.population = population;
        this.envelope = envelope;
        this.area = null;
        this.population_type = "standard_all";
    }

    public PopulationContainerView(PopulationContainer population, Envelope envelope, String population_type,
            int[] population_indizes) {
        this.index = new KdTree();
        this.points = new ArrayList<>();
        List<Integer> indizes = population.getPointsInEnvelop(envelope);
        for (Integer index : indizes) {
            this.index.insert(population.getPoint(index), this.points.size());
            this.points.add(index);
        }
        this.population = population;
        this.envelope = envelope;
        this.area = null;
        if (population_indizes == null && !population_type.equals("standard_all")) {
            throw new IllegalArgumentException("invalid arguments passed to constructor");
        }
        this.population_type = population_type;
        this.population_indizes = population_indizes;
    }

    public PopulationContainerView(PopulationContainer population, Geometry area) {
        this.index = new KdTree();
        this.points = new ArrayList<>();
        List<Integer> indizes = population.getPointsInGeometry(area);
        for (Integer index : indizes) {
            this.index.insert(population.getPoint(index), this.points.size());
            this.points.add(index);
        }
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
        return this.population.getPoint(this.points.get(index));
    }

    public Coordinate getCoordinate(int index, String crs) {
        if (crs.equals("EPSG:4326")) {
            return this.population.getPoint(this.points.get(index));
        } else if (crs.equals("EPSG:25832")) {
            return this.population.getUTMPoint(this.points.get(index));
        }
        return new Coordinate(0, 0);
    }

    public int getPopulation(int index) {
        PopulationAttributes attrs = this.population.getAttributes(this.points.get(index));
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

    public int pointCount() {
        return this.points.size();
    }

    public List<Integer> getPointsInEnvelop(Envelope envelope) {
        Envelope env;
        if (this.envelope == null) {
            env = envelope;
        } else {
            env = this.envelope.intersection(envelope);
        }
        if (env == null) {
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < pointCount(); i++) {
                indices.add(i);
            }
            return indices;
        }

        List<Integer> points = new ArrayList<>();

        this.index.query(env, node -> {
            int index = (int) node.getData();
            points.add(index);
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
