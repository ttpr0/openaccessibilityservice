package org.tud.oas.api.queries.aggregate;

import java.util.UUID;

import org.tud.oas.demand.IDemandView;
import org.tud.oas.routing.ICatchment;

class AggregateQuerySession {
    public UUID id;

    public IDemandView demand_view;

    public ICatchment catchment;

    public AggregateQuerySession(UUID id, IDemandView population_view, ICatchment catchment) {
        this.id = id;
        this.demand_view = population_view;
        this.catchment = catchment;
    }
}