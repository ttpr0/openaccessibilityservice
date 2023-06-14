using System;
using NetTopologySuite.Geometries;

namespace DVAN.Routing.ORS
{
    public class Isochrone
    {
        public Geometry geometry { get; set; }
        public double value { get; set; }
        public Envelope envelope { get; set; }

        public Isochrone(Geometry geometry, double value)
        {
            this.geometry = geometry;
            this.value = value;
        }

        public Envelope getEnvelope()
        {
            if (this.envelope == null)
                this.envelope = this.geometry.EnvelopeInternal;
            return this.envelope;
        }

        public Geometry getGeometry()
        {
            return geometry;
        }

        public double getValue()
        {
            return value;
        }
    }
}
