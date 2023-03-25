using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;

namespace DVAN.API
{
    public class GravityAccessibilityRequest
    {
        public List<double>? ranges { get; set; }

        public List<double>? range_factors { get; set; }

        public double[][]? facility_locations { get; set; }

        public double[]? envelop { get; set; }

        private Envelope? envelope;

        public Envelope getEnvelope() {
            if (this.envelope == null) {
                this.envelope = new Envelope(this.envelop[0], this.envelop[2], this.envelop[1], this.envelop[3]);
            }
            return envelope;
        }
    }
}