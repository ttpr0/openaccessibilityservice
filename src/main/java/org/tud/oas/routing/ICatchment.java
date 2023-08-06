package org.tud.oas.routing;

public interface ICatchment {
    Iterable<Integer> getNeighbours(int destination);
}