using System;
using System.Collections.Generic;

namespace DVAN.Accessibility
{
    public class PopulationAccessibility {
        public List<int> ranges;

        public PopulationAccessibility() {
            this.ranges = new List<int>();
        }

        public void addRange(int range) {
            this.ranges.Add(range);
        }
    }
}