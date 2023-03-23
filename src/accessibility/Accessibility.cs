using System;
using System.Collections.Generic;

namespace DVAN.Accessibility
{
    public class Accessibility {
        public PopulationAccessibility[] accessibilities;
        public FacilityCatchment[] catchments;
        
        public Accessibility(PopulationAccessibility[] accessibilities, FacilityCatchment[] catchments) {
            this.accessibilities = accessibilities;
            this.catchments = catchments;
        }
    }
}