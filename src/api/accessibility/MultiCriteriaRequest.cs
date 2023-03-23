using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;

namespace DVAN.API
{
    public class MultiCriteriaRequest 
    {
        public Dictionary<string, InfrastructureParams> infrastructures { get; set; }

        public double[] envelop { get; set; }

        public string population_type { get; set; }

        public int[]? population_indizes { get; set; }

        private Envelope envelope;

        public Dictionary<string, InfrastructureParams> getInfrastructures() {
            return infrastructures;
        }

        public void setInfrastructures(Dictionary<string, InfrastructureParams> infrastructures) {
            this.infrastructures = infrastructures;
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

        public double[] getEnvelop() {
            return envelop;
        }

        public void setEnvelop(double[] envelop) {
            this.envelop = envelop;
        }

        public String getPopulationType() {
            return population_type;
        }

        public void setPopulationType(String populationType) {
            this.population_type = populationType;
        }

        public int[] getPopulationIndizes() {
            return population_indizes;
        }

        public void setPopulationIndizes(int[] populationIndizes) {
            this.population_indizes = populationIndizes;
        }
    }

    public class InfrastructureParams 
    {
        public double infrastructure_weight { get; set; }

        public List<Double> ranges { get; set; }

        public List<Double> range_factors { get; set; }

        public Double[][] facility_locations { get; set; }
    }
}