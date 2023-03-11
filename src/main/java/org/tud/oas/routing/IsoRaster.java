package org.tud.oas.routing;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdTree;

public class IsoRaster {
    public String type = "Raster";
    public double precession;
    public String crs;
    public double[] extend;
    public int[] size;
    public List<GridFeature> features;

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

    public void constructIndex() {
        this.index = new KdTree(10);
        for (GridFeature feature : this.features) {
            this.index.insert(new Coordinate(feature.x, feature.y), feature.value);
        }
    }

    public int getValueAtCoordinate(Coordinate coord) {
        KdNode node = this.index.query(coord);
        if (node == null) {
            return -1;
        }
        GridValue value = (GridValue)node.getData();
        return value.range;
    }
}

class GridValue
{
    public int range;

    public GridValue(int value) {
        this.range = value;
    }
}

class GridFeature {
    public float x;
    public float y;
    public GridValue value;
    
    public GridFeature(float x, float y, GridValue value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }
}