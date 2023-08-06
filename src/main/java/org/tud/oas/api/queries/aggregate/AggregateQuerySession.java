package org.tud.oas.api.queries.aggregate;

import java.util.UUID;

import org.tud.oas.population.IPopulationView;
import org.tud.oas.routing.ICatchment;

class AggregateQuerySession {
    public UUID id;

    public IPopulationView population_view;

    public ICatchment catchment;

    public AggregateQuerySession(UUID id, IPopulationView population_view, ICatchment catchment) {
        this.id = id;
        this.population_view = population_view;
        this.catchment = catchment;
    }
}