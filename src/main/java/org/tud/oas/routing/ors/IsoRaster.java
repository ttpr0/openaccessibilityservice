package org.tud.oas.routing.ors;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdTree;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IsoRaster {
    private int id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("precession")
    private double precession;

    @JsonProperty("crs")
    private String crs;

    @JsonProperty("extend")
    private double[] extend;

    @JsonProperty("size")
    private int[] size;

    @JsonProperty("envelope")
    private double[][] envelope;

    @JsonProperty("features")
    private List<GridFeature> features;

    private KdTree index;

    private String error;

    public IsoRaster(String error) {
        this.error = error;
    }

    public boolean isNull() {
        return error != null;
    }

    public String getError() {
        return error;
    }

    public int getID() {
        return this.id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public double getPrecession() {
        return precession;
    }

    public String getCrs() {
        return crs;
    }

    public double[] getExtend() {
        return extend;
    }

    public int[] getSize() {
        return size;
    }

    public double[][] getEnvelope() {
        return envelope;
    }

    public void constructIndex() {
        index = new KdTree();
        for (GridFeature feature : features) {
            index.insert(new Coordinate(feature.getX(), feature.getY()), feature.getValue());
        }
    }

    private Envelope _envelope = new Envelope();

    public Map<Integer, Integer> getValueAtCoordinate(Coordinate coord) {
        _envelope.init(coord.getX() - 1000, coord.getX() + 1000, coord.getY() - 1000, coord.getY() + 1000);
        List<KdNode> nodes = index.query(_envelope);
        if (nodes.isEmpty()) {
            return null;
        }
        return (Map<Integer, Integer>) nodes.get(0).getData();
    }

    public IsoRasterAccessor getAccessor(Coordinate coord) {
        _envelope.init(coord.getX() - 1000, coord.getX() + 1000, coord.getY() - 1000, coord.getY() + 1000);
        List<KdNode> nodes = index.query(_envelope);
        if (nodes.isEmpty()) {
            return null;
        }
        return new IsoRasterAccessor(coord, nodes);
    }
}

class IsoRasterAccessor {
    private float[] factors;
    private Map<Integer, Integer>[] rangeDicts;
    private HashSet<Integer> facilities;

    public IsoRasterAccessor(Coordinate coord, List<KdNode> nodes) {
        int numNodes = nodes.size();
        factors = new float[numNodes];
        rangeDicts = new Map[numNodes];
        facilities = new HashSet<>();

        for (int i = 0; i < numNodes; i++) {
            KdNode node = nodes.get(i);
            Map<Integer, Integer> value = (Map<Integer, Integer>) node.getData();
            Coordinate nodeCoord = node.getCoordinate();
            factors[i] = (float) coord.distance(nodeCoord);
            rangeDicts[i] = value;
            facilities.addAll(value.keySet());
        }
    }

    public HashSet<Integer> getFacilities() {
        return facilities;
    }

    public float getRange(int facility) {
        float range = 0;
        float factorSum = 0;
        for (int i = 0; i < factors.length; i++) {
            Map<Integer, Integer> dict = rangeDicts[i];
            float factor = factors[i];
            if (dict.containsKey(facility)) {
                factorSum += factor;
                range += dict.get(facility) * factor;
            }
        }
        return range / factorSum;
    }
}

class GridFeature {
    private float x;
    private float y;
    private Map<Integer, Integer> value;

    public GridFeature() {
    }

    public GridFeature(float x, float y, Map<Integer, Integer> value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Map<Integer, Integer> getValue() {
        return value;
    }

    public void setValue(Map<Integer, Integer> value) {
        this.value = value;
    }
}

class GridValue {
    public int range;

    public GridValue() {
    }

    public GridValue(int value) {
        this.range = value;
    }
}
