using System;
using System.Collections.Generic;
using DVAN.Accessibility;
using NetTopologySuite.Geometries;

namespace DVAN.API
{
    class NearestQuerySession
    {
        public Guid id { get; set; }

        public NearestQueryParameters parameters { get; set; }

        public List<RangeRef>[] accessibilities { get; set; }

        public int[] computed_values { get; set; }
    }
}