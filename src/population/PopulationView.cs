using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;
using NetTopologySuite.Index.KdTree;
using NetTopologySuite.Algorithm.Locate;

namespace DVAN.Population
{
    public class PopulationView {
        PopulationContainer population;
        Geometry? area;
        Envelope envelope;
        String population_type;
        int[] population_indizes;

        public PopulationView(PopulationContainer population, Envelope envelope) {
            this.population = population;
            this.envelope = envelope;
            this.area = null;
            this.population_type = "standard_all";
        }

        public PopulationView(PopulationContainer population, Envelope envelope, String population_type, int[] population_indizes) {
            this.population = population;
            this.envelope = envelope;
            this.area = null;
            this.population_type = population_type;
            this.population_indizes = population_indizes;
        }

        public PopulationView(PopulationContainer population, Geometry area) {
            this.population = population;
            this.area = area;
            this.envelope = area.EnvelopeInternal;
        }

        public Envelope getEnvelope() {
            return this.envelope;
        }

        public Coordinate getCoordinate(int index) {
            return this.population.points[index];
        }

        public Coordinate getCoordinate(int index, String crs) {
            if (crs == "EPSG:4326") {
                return this.population.points[index];
            } else if (crs == "EPSG:25832") {
                return this.population.utm_points[index];
            }
            return new Coordinate(0, 0);
        }

        public int getPopulationCount(int index) {
            PopulationAttributes attrs = this.population.attributes[index];
            if (this.population_type == null || this.population_type.Equals("standard_all")) {
                return attrs.getPopulationCount();
            } 
            if (this.population_type.Equals("standard")) {
                return attrs.getStandardPopulation(population_indizes);
            }
            if (this.population_type.Equals("kita_schul")) {
                return attrs.getKitaSchulPopulation(population_indizes);
            }
            return 0;
        }

        public List<int> getAllPoints() {
            List<int> points = new List<int>(100);

            var visitor = new VisitKdNode<object>();
            visitor.setFunc((KdNode<object> node) => {
                int index = (int)node.Data;
                points.Add(index);
            });

            this.population.index.Query(this.envelope, visitor);

            return points;
        }

        public List<int> getPointsInEnvelop(Envelope envelope) {
            List<int> points = new List<int>(100);

            Envelope env = this.envelope.Intersection(envelope);
            if (env == null) {
                return points;
            }

            var visitor = new VisitKdNode<object>();
            visitor.setFunc((node) => {
                if (this.area == null) {
                    int index = (int)node.Data;
                    points.Add(index);
                } else {
                    Location location = SimplePointInAreaLocator.Locate(node.Coordinate, this.area);
                    if (location == Location.Interior) {
                        int index = (int)node.Data;
                        points.Add(index);
                    }
                }
            });

            this.population.index.Query(env, visitor);

            return points;
        }

        public String getPopulationType() {
            return population_type;
        }

        public void setPopulationType(String population_type) {
            this.population_type = population_type;
        }

        public int[] getPopulationIndizes() {
            return population_indizes;
        }

        public void setPopulationIndizes(int[] population_indizes) {
            this.population_indizes = population_indizes;
        }
    }
}