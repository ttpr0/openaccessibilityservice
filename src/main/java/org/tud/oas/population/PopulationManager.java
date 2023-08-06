package org.tud.oas.population;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.kdtree.KdTree;

public class PopulationManager {
    private static PopulationContainer population;

    private static Map<UUID, Pair<IPopulationView, Date>> stored_views = new ConcurrentHashMap<>();

    public static void loadPopulation(String filename) {
        try {
            PopulationManager.population = PopulationLoader.loadFromCSV(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PopulationContainer getPopulation() {
        return PopulationManager.population;
    }

    public static IPopulationView getPopulationView(PopulationRequestParams param) {
        IPopulationView view;
        try {
            if (param.population_id != null) {
                view = PopulationManager.getStoredPopulationView(param.population_id);
                if (view != null) {
                    return view;
                }
            }
            if (param.population_locations != null && param.population_weights != null) {
                Envelope envelope;
                if (param.envelop != null) {
                    envelope = new Envelope(param.envelop[0], param.envelop[2], param.envelop[1], param.envelop[3]);
                } else {
                    envelope = null;
                }
                view = PopulationManager.createPopulationView(param.population_locations, param.population_weights,
                        envelope);
                if (view != null) {
                    return view;
                }
            }
            if (true) {
                if (param.envelop != null) {
                    Envelope envelope = new Envelope(param.envelop[0], param.envelop[2], param.envelop[1],
                            param.envelop[3]);
                    if (param.population_type != null) {
                        view = PopulationManager.createPopulationView(envelope, param.population_type,
                                param.population_indizes);
                    } else {
                        view = PopulationManager.createPopulationView(envelope);
                    }
                    if (view != null) {
                        return view;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static IPopulationView createPopulationView(Envelope envelope) {
        return new PopulationContainerView(population, envelope);
    }

    public static IPopulationView createPopulationView(Envelope envelope, String type, int[] indizes) {
        return new PopulationContainerView(population, envelope, type, indizes);
    }

    public static IPopulationView createPopulationView(Geometry area) {
        return new PopulationContainerView(population, area);
    }

    public static IPopulationView createPopulationView(double[][] locations, double[] weights, Envelope envelop) {
        List<Coordinate> points = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (int i = 0; i < locations.length; i++) {
            double[] location = locations[i];
            points.add(new Coordinate(location[0], location[1]));
            counts.add((int) (weights[i]));
        }
        return new PopulationView(points, null, counts, envelop);
    }

    public static IPopulationView createPopulationView(double[][] locations, Envelope envelop) {
        List<Coordinate> points = new ArrayList<>();
        for (int i = 0; i < locations.length; i++) {
            double[] location = locations[i];
            points.add(new Coordinate(location[0], location[1]));
        }
        return new PopulationView(points, null, null, envelop);
    }

    public static UUID storePopulationView(IPopulationView view) {
        UUID id = UUID.randomUUID();
        stored_views.put(id, new Pair<>(view, new Date()));
        return id;
    }

    public static IPopulationView getStoredPopulationView(UUID id) {
        Pair<IPopulationView, Date> pair = stored_views.get(id);
        if (pair != null) {
            pair.setSecond(new Date()); // Update the timestamp
            return pair.getFirst();
        }
        return null;
    }

    public static void periodicClearViewStore(long run_interval_ms, long del_interval_ms) {
        Thread thread = new Thread(() -> {
            while (true) {
                List<UUID> to_delete = new ArrayList<>();
                long curr = System.currentTimeMillis();
                for (Map.Entry<UUID, Pair<IPopulationView, Date>> entry : stored_views.entrySet()) {
                    Date time = entry.getValue().getSecond();
                    if ((curr - time.getTime()) > del_interval_ms) {
                        to_delete.add(entry.getKey());
                    }
                }
                for (UUID id : to_delete) {
                    stored_views.remove(id);
                }
                try {
                    Thread.sleep(run_interval_ms);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private static class Pair<A, B> {
        private A first;
        private B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public A getFirst() {
            return first;
        }

        public void setFirst(A first) {
            this.first = first;
        }

        public B getSecond() {
            return second;
        }

        public void setSecond(B second) {
            this.second = second;
        }
    }
}
