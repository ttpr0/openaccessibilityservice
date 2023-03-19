package org.tud.oas.routing;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdTree;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IsoRaster {
    @JsonProperty("type")
    public String type = "Raster";

    @JsonProperty("precession")
    public double precession;

    @JsonProperty("crs")
    public String crs;

    @JsonProperty("extend")
    public double[] extend;

    @JsonProperty("size")
    public int[] size;

    @JsonProperty("envelope")
    public double[][] envelop;

    @JsonProperty("features")
    public List<GridFeature> features;

    public IsoRaster() {
    }

    private KdTree index;

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
        return this.envelop;
    }

    public void constructIndex() {
        this.index = new KdTree(500);
        for (GridFeature feature : this.features) {
            this.index.insert(new Coordinate(feature.x, feature.y), feature.value);
        }
    }

    Envelope envelope = new Envelope();

    public int getValueAtCoordinate(Coordinate coord) {
        this.envelope.init(coord.x-500, coord.x+500, coord.y-500, coord.y+500);
        List<KdNode> nodes = this.index.query(this.envelope);
        if (nodes.size() == 0) {
            return -1;
        }
        GridValue value = (GridValue)nodes.get(0).getData();
        return value.range;
    }
}