using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;
using NetTopologySuite.Index.KdTree;

namespace DVAN.Population
{
    public class PopulationContainer
    {
        public KdTree<object> index;

        public List<Coordinate> points;

        public List<Coordinate> utm_points;

        public List<PopulationAttributes> attributes;

        public PopulationContainer(int initial_size)
        {
            this.index = new KdTree<object>();
            this.points = new List<Coordinate>(initial_size);
            this.utm_points = new List<Coordinate>(initial_size);
            this.attributes = new List<PopulationAttributes>(initial_size);
        }

        public void addPopulationPoint(Coordinate point, Coordinate utm_point, PopulationAttributes attributes)
        {
            int index = this.points.Count;
            attributes.setIndex(index);
            this.points.Add(point);
            this.utm_points.Add(utm_point);
            this.attributes.Add(attributes);
            this.index.Insert(point, index);
        }

        public Coordinate getPoint(int index)
        {
            return this.points[index];
        }

        public Coordinate getUTMPoint(int index)
        {
            return this.utm_points[index];
        }

        public PopulationAttributes getAttributes(int index)
        {
            return this.attributes[index];
        }

        public int getPointCount()
        {
            return this.points.Count;
        }

        public List<int> getPointsInEnvelop(Envelope envelope)
        {
            List<int> points = new List<int>(100);

            var visitor = new VisitKdNode<object>();
            visitor.setFunc((KdNode<object> node) => {
                int index = (int)node.Data;
                points.Add(index);
            });

            this.index.Query(envelope, visitor);

            return points;
        }
    }

    class VisitKdNode<T> : IKdNodeVisitor<T> where T : class
    {
        Action<KdNode<T>> func;

        internal void setFunc(Action<KdNode<T>> func)
        {
            this.func = func;
        }

        public void Visit(KdNode<T> node)
        {
            this.func(node);
        }
    }
}