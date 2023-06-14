using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading.Tasks.Dataflow;
using DVAN.Population;

namespace DVAN.Routing
{
    public interface IRoutingProvider
    {
        void setProfile(string profile);
        void setRangeType(string range_type);
        void setOption(string name, object value);

        /// <summary>
        /// Computes the time-distance-matrix containing travel-times between all facilities and population points.
        /// </summary>
        Task<ITDMatrix?> requestTDMatrix(IPopulationView population, double[][] facilities, List<double> ranges, string mode);

        /// <summary>
        /// Computes the nearest facilities to the population points.
        /// </summary>
        Task<INNTable?> requestNearest(IPopulationView population, double[][] facilities, List<double> ranges, string mode);

        /// <summary>
        /// Computes the k-nearest facilities to the population points.
        /// </summary>
        Task<IKNNTable?> requestKNearest(IPopulationView population, double[][] facilities, List<double> ranges, int k, string mode);


        /// <summary>
        /// Computes all facilities that lie within a given range to the population points.
        /// </summary>
        Task<ICatchment?> requestCatchment(IPopulationView population, double[][] facilities, double range, string mode);
    }
}