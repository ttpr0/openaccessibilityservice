using System;
using System.Collections.Generic;

namespace DVAN.Routing
{
    public interface ICatchment
    {
        IEnumerable<int> getNeighbours(int destination);
    }
}