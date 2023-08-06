package org.tud.oas.api.fca;

import org.tud.oas.population.PopulationRequestParams;
import org.tud.oas.accessibility.distance_decay.DecayRequestParams;
import org.tud.oas.routing.RoutingRequestParams;

import org.locationtech.jts.geom.Coordinate;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

class FCARequest {
    @JsonProperty("ranges")
    public List<Double> ranges;

    @JsonProperty("population")
    public PopulationRequestParams population;

    @JsonProperty("distance_decay")
    public DecayRequestParams distance_decay;

    @JsonProperty("routing")
    public RoutingRequestParams routing;

    @JsonProperty("facility_locations")
    public double[][] facility_locations;

    @JsonProperty("facility_capacities")
    public double[] facility_capacities;

    @JsonProperty("mode")
    public String mode;
}
