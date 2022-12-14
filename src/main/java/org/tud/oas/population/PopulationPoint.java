package org.tud.oas.population;

import com.vividsolutions.jts.geom.Point;

public class PopulationPoint {
    private Point point;
    private PopulationAttributes attributes;
    private int weight;
    
    public PopulationPoint(Point point, PopulationAttributes attributes) {
        this.point = point;
        this.attributes = attributes;
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
}
