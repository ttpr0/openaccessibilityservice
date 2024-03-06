package org.tud.oas.routing.ors;

import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

public class IsochroneCollection {
    private int id;
    private Envelope envelope;
    private List<Isochrone> isochrones;
    private Coordinate center;
    private String error;

    public IsochroneCollection(int id, Envelope envelope, List<Isochrone> isochrones, Coordinate center) {
        this.id = id;
        this.envelope = envelope;
        this.isochrones = isochrones;
        this.center = center;
        this.error = null;
    }

    public IsochroneCollection(String error) {
        this.error = error;
    }

    public boolean isNull() {
        return this.error != null;
    }

    public String getError() {
        return this.error;
    }

    public int getID() {
        return this.id;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public List<Isochrone> getIsochrones() {
        return isochrones;
    }

    public Isochrone getIsochrone(int index) {
        return this.isochrones.get(index);
    }

    public int getIsochronesCount() {
        return this.isochrones.size();
    }

    public Coordinate getCenter() {
        return center;
    }
}
