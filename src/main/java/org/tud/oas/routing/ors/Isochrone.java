package org.tud.oas.routing.ors;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

public class Isochrone {
    public Geometry geometry;
    public double value;
    public Envelope envelope;

    public Isochrone(Geometry geometry, double value) {
        this.geometry = geometry;
        this.value = value;
    }

    public Envelope getEnvelope() {
        if (this.envelope == null)
            this.envelope = this.geometry.getEnvelopeInternal();
        return this.envelope;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public double getValue() {
        return value;
    }
}