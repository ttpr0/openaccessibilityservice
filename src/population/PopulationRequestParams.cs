using System;

namespace DVAN.Population
{
    /// <summary>
    /// Population Request Parameters.
    /// Depending on which parameters are not null population view will be created differently.
    /// If "population_id" is not null => stored view will be used.
    /// If "population_locations" and "population_weights" not null => locations and weights will be used.
    /// Else => envelope and internaly stored population dataset will be used.
    /// </summary>
    public class PopulationRequestParams
    {
        //*************************************
        // stored view
        //*************************************
        /// <summary>
        /// ID of stored population view.
        /// </summary>
        public Guid? population_id { get; set; }

        //*************************************
        // create new view with locations and weights
        //*************************************
        /// <summary>
        /// Locations of population points (in geographic coordinates).
        /// </summary>
        /// <example>[[9.11, 51.23], [9.35, 50.98], [10.02, 52.10]]</example>
        public double[][]? population_locations { get; set; }
        /// <summary>
        /// Weights of population points (e.g. population count).
        /// Weight for every point in "population_locations".
        /// </summary>
        /// <example>[91, 34, 72]</example>
        public double[]? population_weights { get; set; }

        //**************************************
        // create new view from internal population data
        //**************************************
        /// <summary>
        /// Envelope for which population data should be used.
        /// [minx, miny, maxx, maxy]
        /// </summary>
        /// <example>[9.11, 50.98, 10.23, 52.09]</example>
        public double[]? envelop { get; set; }
        /// <summary>
        /// Type of population to be used (one of "standard_all", "standard", "kita_schul").
        /// Specifies how "population_indizes" should be interpreted.
        /// Defaults to "standard_all".
        /// </summary>
        /// <example>standard_all</example>
        public string? population_type { get; set; }
        /// <summary>
        /// Gives the indizes of population data (DVAN) to be included in population count.
        /// E.g. for 20-39 and 40-59 => [2, 3]
        /// </summary>
        /// <example>[2, 3]</example>
        public int[]? population_indizes { get; set; }
    }
}
