using System;
using System.Collections.Generic;

namespace DVAN.Routing
{
    public interface INNTable
    {
        (int, float) getNearest(int destination);
    }
}