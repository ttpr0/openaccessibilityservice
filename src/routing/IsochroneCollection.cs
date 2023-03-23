using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;

namespace DVAN.Routing 
{
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
            return this.isochrones[index];
        }

        public int getIsochronesCount() {
            return this.isochrones.Count;
        }

        public Coordinate getCenter() {
            return center;
        }
    }
}
