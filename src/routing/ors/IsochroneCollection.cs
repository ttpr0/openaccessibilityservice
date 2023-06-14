using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;

namespace DVAN.Routing.ORS
{
    public class IsochroneCollection
    {
        private int id;
        private Envelope envelope;
        private List<Isochrone> isochrones;
        private Coordinate center;

        public IsochroneCollection(int id, Envelope envelope, List<Isochrone> isochrones, Coordinate center)
        {
            this.id = id;
            this.envelope = envelope;
            this.isochrones = isochrones;
            this.center = center;
        }

        public int getID()
        {
            return this.id;
        }

        public Envelope getEnvelope()
        {
            return envelope;
        }

        public List<Isochrone> getIsochrones()
        {
            return isochrones;
        }

        public Isochrone getIsochrone(int index)
        {
            return this.isochrones[index];
        }

        public int getIsochronesCount()
        {
            return this.isochrones.Count;
        }

        public Coordinate getCenter()
        {
            return center;
        }
    }
}
