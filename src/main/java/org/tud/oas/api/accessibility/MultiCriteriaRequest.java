package org.tud.oas.api.accessibility;

import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.locationtech.jts.geom.Envelope;

public class MultiCriteriaRequest {
    @JsonProperty("infrastructures")
    private HashMap<String, InfrastructureParams> infrastructures;

    @JsonProperty("envelop")
    private double[] envelop;

    @JsonIgnore
    private Envelope envelope;

    public HashMap<String, InfrastructureParams> getInfrastructures() {
        return infrastructures;
    }

    public void setInfrastructures(HashMap<String, InfrastructureParams> infrastructures) {
        this.infrastructures = infrastructures;
    }

    public Envelope getEnvelope() {
        if (this.envelope == null) {
            this.envelope = new Envelope(this.envelop[0], this.envelop[2], this.envelop[1], this.envelop[3]);
        }
        return envelope;
    }

    public void setEnvelope(Envelope envelope) {
        this.envelope = envelope;
    }

    public double[] getEnvelop() {
        return envelop;
    }

    public void setEnvelop(double[] envelop) {
        this.envelop = envelop;
    }
}

class InfrastructureParams {
    @JsonProperty("infrastructure_weight")
    private double weight;

    @JsonProperty("ranges")
    private List<Double> ranges;

    @JsonProperty("range_factors")
    private List<Double> factors;

    @JsonProperty("facility_locations")
    private Double[][] locations;

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public List<Double> getRanges() {
        return ranges;
    }

    public void setRanges(List<Double> ranges) {
        this.ranges = ranges;
    }

    public List<Double> getFactors() {
        return factors;
    }

    public void setFactors(List<Double> factors) {
        this.factors = factors;
    }

    public Double[][] getLocations() {
        return locations;
    }

    public void setLocations(Double[][] locations) {
        this.locations = locations;
    }
}