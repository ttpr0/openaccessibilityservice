package org.tud.oas.routing;

public interface INNTable {
    int getNearest(int destination);

    float getNearestRange(int destination);
}