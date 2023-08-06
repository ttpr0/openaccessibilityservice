package org.tud.oas.api.queries.n_nearest;

import java.util.UUID;

import org.tud.oas.demand.IDemandView;
import org.tud.oas.routing.IKNNTable;

class NNearestQuerySession {
    public UUID id;

    public IDemandView demand_view;

    public IKNNTable table;

    NNearestQuerySession(UUID id, IDemandView view, IKNNTable table) {
        this.id = id;
        this.demand_view = view;
        this.table = table;
    }
}
