using System;

namespace DVAN.Population
{
    public class PopulationManager
    {
        private static PopulationContainer population;

        public static void loadPopulation(String filename)
        {
            PopulationManager.population = PopulationLoader.loadFromCSV(filename);
        }

        public static PopulationContainer getPopulation()
        {
            return PopulationManager.population;
        }
    }
}