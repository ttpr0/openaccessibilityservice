package org.tud.oas.population;

import org.locationtech.jts.geom.Point;

public class PopulationPoint {
    private Point point;
    private float x;
    private float y;
    private PopulationAttributes attributes;
    private int weight;
    
    public PopulationPoint(Point point, float x, float y, PopulationAttributes attributes) {
        this.point = point;
        this.attributes = attributes;
        this.x = x;
        this.y = y;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public PopulationAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(PopulationAttributes attributes) {
        this.attributes = attributes;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
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
}
