using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;

namespace DVAN.API
{
    public class GravityAccessibilityRequest
    {
        public List<Double>? ranges { get; set; }

        public List<Double>? range_factors { get; set; }

        public Double[][]? facility_locations { get; set; }

        public double[]? envelop { get; set; }

        private Envelope? envelope;

        public List<Double> getRanges() {
            return ranges;
        }

        public void setRanges(List<Double> ranges) {
            this.ranges = ranges;
        }

        public List<Double> getFactors() {
            return range_factors;
        }

        public void setFactors(List<Double> factors) {
            this.range_factors = factors;
        }

        public Double[][] getLocations() {
            return facility_locations;
        }

        public void setLocations(Double[][] locations) {
            this.facility_locations = locations;
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
}