using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading.Tasks.Dataflow;

namespace DVAN.Routing
{
    public interface ITDMatrix
    {
        float getRange(int source, int destination);
    }
}