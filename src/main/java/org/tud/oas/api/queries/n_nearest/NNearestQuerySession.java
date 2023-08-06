package org.tud.oas.api.queries.n_nearest;

import java.util.UUID;

import org.tud.oas.population.IPopulationView;
import org.tud.oas.routing.IKNNTable;

class NNearestQuerySession {
    public UUID id;

    public IPopulationView population_view;

    public IKNNTable table;

    NNearestQuerySession(UUID id, IPopulationView view, IKNNTable table) {
        this.id = id;
        this.population_view = view;
        this.table = table;
    }
}
