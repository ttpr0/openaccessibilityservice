package org.tud.oas.routing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import java.util.List;

public class IsochroneCollection {
    private Envelope envelope;
	private List<Isochrone> isochrones;
	private Coordinate center;
    
    public IsochroneCollection(Envelope envelope, List<Isochrone> isochrones, Coordinate center) {
        this.envelope = envelope;
        this.isochrones = isochrones;
        this.center = center;
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
