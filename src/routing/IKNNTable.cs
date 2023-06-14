using System;
using System.Collections.Generic;

namespace DVAN.Routing
{
    public interface IKNNTable
    {
        (int, float) getKNearest(int destination, int k);
    }
}