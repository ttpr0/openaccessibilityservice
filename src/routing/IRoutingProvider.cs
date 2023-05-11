using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading.Tasks.Dataflow;

namespace DVAN.Routing
{
    public interface IRoutingProvider
    {
        void setProfile(string profile);
        void setRangeType(string range_type);
        void setOption(string name, object value);

        Task<List<IsochroneCollection>> requestIsochrones(double[][] locations, List<double> ranges);

        ISourceBlock<IsochroneCollection> requestIsochronesStream(double[][] locations, List<double> ranges);

        Task<IsoRaster> requestIsoRaster(double[][] locations, double max_range);

        ISourceBlock<IsoRaster> requestIsoRasterStream(double[][] locations, double max_range);

        Task<Matrix> requestMatrix(double[][] sources, double[][] destinations);
    }
}