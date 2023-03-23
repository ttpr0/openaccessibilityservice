using System;
using System.Collections.Generic;

namespace DVAN.API
{
    public class SimpleAccessibilityRequest
    {
        public List<Double> ranges { get; set; }

        public Double[][] facility_locations { get; set; }

        public List<Double> getRanges() {
            return ranges;
        }

        public void setRanges(List<Double> ranges) {
            this.ranges = ranges;
        }

        public Double[][] getLocations() {
            return facility_locations;
        }

        public void setLocations(Double[][] locations) {
            this.facility_locations = locations;
        }
    }
}