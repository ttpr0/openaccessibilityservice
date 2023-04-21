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

        public static void loadPopulation(string filename)
        {
            PopulationManager.population = PopulationLoader.loadFromCSV(filename);
        }

        public static PopulationContainer getPopulation()
        {
            return PopulationManager.population;
        }

        public static IPopulationView? getPopulationView(PopulationRequestParams param)
        {
            IPopulationView view;
            try {
                if (param.population_id != null) {
                    view = PopulationManager.getStoredPopulationView(param.population_id.Value);
                    if (view != null) {
                        return view;
                    }
                }
                if (param.population_locations != null && param.population_weights != null) {
                    Envelope envelope;
                    if (param.envelop != null) {
                        envelope = new Envelope(param.envelop[0], param.envelop[2], param.envelop[1], param.envelop[3]);
                    }
                    else {
                        envelope = null;
                    }
                    view = PopulationManager.createPopulationView(param.population_locations, param.population_weights, envelope);
                    if (view != null) {
                        return view;
                    }
                }
                if (true) {
                    if (param.envelop != null) {
                        var envelope = new Envelope(param.envelop[0], param.envelop[2], param.envelop[1], param.envelop[3]);
                        if (param.population_type != null) {
                            view = PopulationManager.createPopulationView(envelope, param.population_type, param.population_indizes);
                        }
                        else {
                            view = PopulationManager.createPopulationView(envelope);
                        }
                        if (view != null) {
                            return view;
                        }
                    }
                }
            }
            catch { }
            return null;
        }

        public static IPopulationView createPopulationView(Envelope? envelope)
        {
            return new PopulationContainerView(population, envelope);
        }

        public static IPopulationView createPopulationView(Envelope? envelope, string type, int[] indizes)
        {
            return new PopulationContainerView(population, envelope, type, indizes);
        }

        public static IPopulationView createPopulationView(Geometry? area)
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
    }
}