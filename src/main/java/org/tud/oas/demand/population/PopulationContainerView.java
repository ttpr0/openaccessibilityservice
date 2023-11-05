package org.tud.oas.demand.population;

import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.kdtree.KdTree;
import org.tud.oas.demand.IDemandView;

public class PopulationContainerView implements IDemandView {
    private KdTree index;
    private List<Integer> points;
    private PopulationContainer population;
    private Envelope envelope;
    private int[] population_indizes;
    private float[] population_factors;

    public PopulationContainerView(PopulationContainer population, Envelope envelope, int[] population_indizes,
            float[] population_factors) {
        this.index = new KdTree();
        this.points = new ArrayList<>();
        List<Integer> indizes = population.getPointsInEnvelop(envelope);
        for (Integer index : indizes) {
            this.index.insert(population.getPoint(index), this.points.size());
            this.points.add(index);
        }
        this.population = population;
        this.envelope = envelope;
        this.population_indizes = population_indizes;
        this.population_factors = population_factors;
    }

    public PopulationContainerView(PopulationContainer population, Geometry area, int[] population_indizes,
            float[] population_factors) {
        this.index = new KdTree();
        this.points = new ArrayList<>();
        List<Integer> indizes = population.getPointsInGeometry(area);
        for (Integer index : indizes) {
            this.index.insert(population.getPoint(index), this.points.size());
            this.points.add(index);
        }
        this.population = population;
        if (area != null) {
            this.envelope = area.getEnvelopeInternal();
        }
        this.population_indizes = population_indizes;
        this.population_factors = population_factors;
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

    public int getDemand(int index) {
        PopulationAttributes attrs = this.population.getAttributes(this.points.get(index));
        if (this.population_factors != null) {
            return attrs.getPopulationCount(this.population_indizes, this.population_factors);
        }
        return attrs.getPopulationCount(this.population_indizes);
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
}
