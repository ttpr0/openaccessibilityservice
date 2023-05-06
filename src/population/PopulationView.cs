using System;
using System.Collections.Generic;
using System.Linq;
using NetTopologySuite.Geometries;
using NetTopologySuite.Index.KdTree;
using NetTopologySuite.Algorithm.Locate;

namespace DVAN.Population
{
    public class PopulationView : IPopulationView
    {
        public KdTree<object> index;
        public List<Coordinate> points;
        public List<Coordinate> utm_points;
        public List<int> counts;

        Geometry? area;
        Envelope? envelope;

        public PopulationView(List<Coordinate> points, List<Coordinate> utm_points, List<int> counts, Envelope? envelope)
        {
            this.points = points;
            this.utm_points = utm_points;
            this.counts = counts;
            this.index = new KdTree<object>();
            int i = 0;
            foreach (var coord in points) {
                this.index.Insert(coord, i);
                i++;
            }
            this.envelope = envelope;
            this.area = null;
        }

        public PopulationView(List<Coordinate> points, List<Coordinate> utm_points, List<int> counts, Geometry? area)
        {
            this.points = points;
            this.utm_points = utm_points;
            this.counts = counts;
            this.index = new KdTree<object>();
            int i = 0;
            foreach (var coord in points) {
                this.index.Insert(coord, i);
                i++;
            }
            if (area != null) {
                this.area = area;
                this.envelope = area.EnvelopeInternal;
            }
        }

        public Coordinate getCoordinate(int index)
        {
            return this.points[index];
        }

        public Coordinate getCoordinate(int index, String crs)
        {
            if (crs == "EPSG:4326") {
                return this.points[index];
            }
            else if (crs == "EPSG:25832") {
                return this.utm_points[index];
            }
            return new Coordinate(0, 0);
        }

        public int getPopulation(int index)
        {
            return this.counts[index];
        }

        public int pointCount()
        {
            return this.points.Count;
        }

        public List<int> getPointsInEnvelop(Envelope? envelope)
        {
            Envelope env;
            if (this.envelope == null) {
                env = envelope;
            }
            else {
                env = this.envelope.Intersection(envelope);
            }
            if (env == null) {
                return Enumerable.Range(0, this.pointCount()).ToList();
            }

            List<int> points = new List<int>(100);
            var visitor = new VisitKdNode<object>();
            visitor.setFunc((node) => {
                if (this.area == null) {
                    int index = (int)node.Data;
                    points.Add(index);
                }
                else {
                    Location location = SimplePointInAreaLocator.Locate(node.Coordinate, this.area);
                    if (location == Location.Interior) {
                        int index = (int)node.Data;
                        points.Add(index);
                    }
                }
            });

            this.index.Query(env, visitor);

            return points;
        }
    }
}