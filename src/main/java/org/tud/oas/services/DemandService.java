package org.tud.oas.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tud.oas.config.OASProperties;
import org.tud.oas.demand.DemandView;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.demand.population.PopulationManager;
import org.tud.oas.requests.DemandRequestParams;
import org.tud.oas.util.Pair;

@Service
public class DemandService {
    private static Map<UUID, Pair<IDemandView, Date>> stored_views = new ConcurrentHashMap<>();

    @Autowired
    public DemandService(OASProperties props) {
        var demand_props = props.getDemand();
        PopulationManager.loadPopulation("dvan_population", demand_props.getPopulationFile());
        DemandService.periodicClearViewStore(demand_props.getRunInterval() * 60 * 1000,
                demand_props.getDeleteInterval() * 60 * 1000);
    }

    public IDemandView getDemandView(DemandRequestParams param) {
        IDemandView view;
        try {
            if (param.view_id != null) {
                view = this.getStoredDemandView(param.view_id);
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
                view = this.createDemandView(param.demand_locations, param.demand_weights,
                        envelope);
                if (view != null) {
                    return view;
                }
            }
            if (true) {
                if (param.area != null) {
                    GeometryFactory geom_factory = new GeometryFactory();
                    Coordinate[] coordinates = new Coordinate[param.area.length];
                    for (int j = 0; j < param.area.length; j++) {
                        coordinates[j] = new Coordinate(param.area[j][0], param.area[j][1]);
                    }
                    Geometry area = geom_factory.createPolygon(coordinates);
                    if (param.population_factors != null) {
                        view = PopulationManager.createPopulationView(param.population_name, area,
                                param.population_indizes, param.population_factors);
                    } else {
                        view = PopulationManager.createPopulationView(param.population_name, area,
                                param.population_indizes);
                    }
                    if (view != null) {
                        return view;
                    }
                }
                if (param.envelop != null) {
                    Envelope envelope = new Envelope(param.envelop[0], param.envelop[2], param.envelop[1],
                            param.envelop[3]);
                    if (param.population_factors != null) {
                        view = PopulationManager.createPopulationView(param.population_name, envelope,
                                param.population_indizes, param.population_factors);
                    } else {
                        view = PopulationManager.createPopulationView(param.population_name, envelope,
                                param.population_indizes);
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

    public IDemandView createDemandView(double[][] locations, double[] weights, Envelope envelop) {
        List<Coordinate> points = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (int i = 0; i < locations.length; i++) {
            double[] location = locations[i];
            points.add(new Coordinate(location[0], location[1]));
            counts.add((int) (weights[i]));
        }
        return new DemandView(points, null, counts);
    }

    public IDemandView createDemandView(double[][] locations, Envelope envelop) {
        List<Coordinate> points = new ArrayList<>();
        for (int i = 0; i < locations.length; i++) {
            double[] location = locations[i];
            points.add(new Coordinate(location[0], location[1]));
        }
        return new DemandView(points, null, null);
    }

    public UUID storeDemandView(IDemandView view) {
        UUID id = UUID.randomUUID();
        DemandService.stored_views.put(id, new Pair<>(view, new Date()));
        return id;
    }

    public IDemandView getStoredDemandView(UUID id) {
        Pair<IDemandView, Date> pair = DemandService.stored_views.get(id);
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
