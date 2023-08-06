package org.tud.oas.routing;

public interface IKNNTable {
    int getKNearest(int destination, int k);

    float getKNearestRange(int destination, int k);
}