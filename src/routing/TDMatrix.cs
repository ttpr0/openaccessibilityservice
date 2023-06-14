using System;
using System.Collections.Generic;

namespace DVAN.Routing
{
    public class TDMatrix : ITDMatrix
    {
        private double[][] durations { get; set; }

        public TDMatrix(double[][] durations)
        {
            this.durations = durations;
        }

        public float getRange(int source, int destination)
        {
            return (float)durations[source][destination];
        }
    }

    public class TDMatrix2 : ITDMatrix
    {
        private float[,] durations { get; set; }

        public TDMatrix2(float[,] durations)
        {
            this.durations = durations;
        }

        public float getRange(int source, int destination)
        {
            return durations[source, destination];
        }
    }
}