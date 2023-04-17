using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using NetTopologySuite.Geometries;
using NetTopologySuite.Index.KdTree;

namespace DVAN.Population
{
    public class PopulationManager
    {
        private static PopulationContainer population;

        private static Dictionary<Guid, (IPopulationView, DateTime)> stored_views = new Dictionary<Guid, (IPopulationView, DateTime)>();

        public static void loadPopulation(String filename)
        {
            PopulationManager.population = PopulationLoader.loadFromCSV(filename);
        }

        public static PopulationContainer getPopulation()
        {
            return PopulationManager.population;
        }

        public static IPopulationView getPopulationView(Envelope? envelope)
        {
            return new PopulationContainerView(population, envelope);
        }

        public static IPopulationView getPopulationView(Envelope? envelope, string type, int[] indizes)
        {
            return new PopulationContainerView(population, envelope, type, indizes);
        }

        public static IPopulationView getPopulationView(Geometry? area)
        {
            return new PopulationContainerView(population, area);
        }

        public static IPopulationView createPopulationView(double[][] locations, double[] weights, Envelope envelop)
        {
            var points = new List<Coordinate>();
            var counts = new List<int>();
            for (int i = 0; i < locations.Length; i++) {
                var location = locations[i];
                points.Add(new Coordinate(location[0], location[1]));
                counts.Add((int)(weights[i]));
            }
            return new PopulationView(points, null, counts, envelop);
        }

        public static IPopulationView createPopulationView(double[][] locations, Envelope envelop)
        {
            var points = new List<Coordinate>();
            for (int i = 0; i < locations.Length; i++) {
                var location = locations[i];
                points.Add(new Coordinate(location[0], location[1]));
            }
            return new PopulationView(points, null, null, envelop);
        }

        public static async Task periodicClearViewStore(TimeSpan run_interval, TimeSpan del_interval)
        {
            while (true) {
                var to_delete = new List<Guid>();
                foreach (var item in stored_views) {
                    var curr = DateTime.UtcNow;
                    var (_, time) = item.Value;
                    if ((curr - time) > del_interval) {
                        to_delete.Add(item.Key);
                    }
                }
                foreach (var item in to_delete) {
                    stored_views.Remove(item);
                }
                await Task.Delay(run_interval);
            }
        }

        public static Guid storePopulationView(IPopulationView view)
        {
            var id = Guid.NewGuid();
            stored_views[id] = (view, DateTime.UtcNow);
            return id;
        }

        public static IPopulationView? getStoredPopulationView(Guid id)
        {
            if (stored_views.ContainsKey(id)) {
                var (view, _) = stored_views[id];
                stored_views[id] = (view, DateTime.UtcNow);
                return view;
            }
            return null;
        }
    }
}