using System;
using System.Collections.Generic;
using System.Linq;
using NetTopologySuite.Geometries;
using NetTopologySuite.Index.KdTree;
using NetTopologySuite.Algorithm.Locate;

namespace DVAN.Population
{
    public interface IPopulationView
    {
        public Coordinate getCoordinate(int index);

        public Coordinate getCoordinate(int index, string crs);

        public int getPopulation(int index);

        public int pointCount();

        public List<int> getPointsInEnvelop(Envelope? envelope);
    }
}