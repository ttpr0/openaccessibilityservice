using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading.Tasks.Dataflow;

namespace DVAN.Routing
{
    public interface IRoutingProvider
    {
        Task<List<IsochroneCollection>> requestIsochrones(Double[][] locations, List<Double> ranges);

        ISourceBlock<IsochroneCollection> requestIsochronesStream(Double[][] locations, List<Double> ranges);

        Task<List<IsoRaster>> requestIsoRasters(Double[][] locations, double max_range);

        ISourceBlock<IsoRaster> requestIsoRasterStream(Double[][] locations, double max_range);

        Task<Matrix> requestMatrix(Double[][] sources, Double[][] destinations);
    }
}