package org.tud.oas.routing;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class Isochrone {
    private Geometry geometry;
    private double value;
    private Envelope envelope;

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
