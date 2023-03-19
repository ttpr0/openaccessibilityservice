package org.tud.oas.api.accessibility;

import java.util.List;

import org.locationtech.jts.geom.Envelope;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GravityAccessibilityRequest {
    @JsonProperty("ranges")
    private List<Double> ranges;

    @JsonProperty("range_factors")
    private List<Double> factors;

    @JsonProperty("facility_locations")
    private Double[][] locations;

    @JsonProperty("envelop")
    private double[] envelop;

    @JsonIgnore
    private Envelope envelope;

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

    public Envelope getEnvelop() {
        return envelope;
    }

    public void setEnvelop(Envelope envelope) {
        this.envelope = envelope;
    }

    public void setEnvelop(double[] envelop) {
        this.envelop = envelop;
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
}
