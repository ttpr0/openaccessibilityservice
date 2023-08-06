package org.tud.oas.demand;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.locationtech.jts.geom.*;
import org.tud.oas.demand.population.PopulationLoader;
import org.tud.oas.util.Pair;

public class DemandManager {
    private static Map<UUID, Pair<IDemandView, Date>> stored_views = new ConcurrentHashMap<>();

    public static IDemandView getDemandView(DemandRequestParams param) {
        IDemandView view;
        try {
            if (param.view_id != null) {
                view = DemandManager.getStoredDemandView(param.view_id);
                if (view != null) {
                    return view;
                }
            }
            if (param.demand_locations != null && param.demand_weights != null) {
                Envelope envelope;
                if (param.envelop != null) {
                    envelope = new Envelope(param.envelop[0], param.envelop[2], param.envelop[1], param.envelop[3]);
                } else {
                    envelope = null;
                }
                view = DemandManager.createDemandView(param.demand_locations, param.demand_weights,
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
                        view = PopulationLoader.createPopulationView(envelope, param.population_type,
                                param.population_indizes);
                    } else {
                        view = PopulationLoader.createPopulationView(envelope);
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

    public static IDemandView createDemandView(double[][] locations, double[] weights, Envelope envelop) {
        List<Coordinate> points = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (int i = 0; i < locations.length; i++) {
            double[] location = locations[i];
            points.add(new Coordinate(location[0], location[1]));
            counts.add((int) (weights[i]));
        }
        return new DemandView(points, null, counts);
    }

    public static IDemandView createDemandView(double[][] locations, Envelope envelop) {
        List<Coordinate> points = new ArrayList<>();
        for (int i = 0; i < locations.length; i++) {
            double[] location = locations[i];
            points.add(new Coordinate(location[0], location[1]));
        }
        return new DemandView(points, null, null);
    }

    public static UUID storeDemandView(IDemandView view) {
        UUID id = UUID.randomUUID();
        stored_views.put(id, new Pair<>(view, new Date()));
        return id;
    }

    public static IDemandView getStoredDemandView(UUID id) {
        Pair<IDemandView, Date> pair = stored_views.get(id);
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
                for (Map.Entry<UUID, Pair<IDemandView, Date>> entry : stored_views.entrySet()) {
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
}
