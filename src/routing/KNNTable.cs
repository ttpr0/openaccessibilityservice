using System;
using System.Collections.Generic;

namespace DVAN.Routing
{
    public class NNTable : INNTable
    {
        private (int, float)[] ranges { get; set; }

        public NNTable((int, float)[] ranges)
        {
            this.ranges = ranges;
        }

        public (int, float) getNearest(int destination)
        {
            return this.ranges[destination];
        }
    }

    public class KNNTable : IKNNTable
    {
        private (int, float)[,] ranges { get; set; }

        public KNNTable((int, float)[,] ranges)
        {
            this.ranges = ranges;
        }

        public (int, float) getKNearest(int destination, int k)
        {
            return this.ranges[destination, k];
        }
    }
}