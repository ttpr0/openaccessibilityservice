using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;

namespace DVAN.API
{
    public class AggregateQueryRequest
    {
        public Guid? session_id { get; set; }

        public Guid? population_id { get; set; }

        public double[][]? population_locations { get; set; }

        public double[]? envelop { get; set; }

        public double[][]? facility_locations { get; set; }

        public double[]? facility_values { get; set; }

        public double? range { get; set; }

        public string compute_type { get; set; }

        private Envelope? envelope;

        public Envelope getEnvelope()
        {
            if (this.envelope == null) {
                this.envelope = new Envelope(this.envelop[0], this.envelop[2], this.envelop[1], this.envelop[3]);
            }
            return envelope;
        }
    }
}