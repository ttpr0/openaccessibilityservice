using System;

namespace DVAN.Population
{
    public class PopulationRequestParams
    {
        // stored view
        public Guid? population_id { get; set; }

        // create new view with locations and weights
        public double[][]? population_locations { get; set; }
        public double[]? population_weights { get; set; }

        // create new view from internal population data
        public double[]? envelop { get; set; }
        public string? population_type { get; set; }
        public int[]? population_indizes { get; set; }
    }
}
