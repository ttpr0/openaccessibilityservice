package org.tud.oas.demand.population;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.locationtech.jts.io.WKBReader;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.util.Pair;

import jakarta.el.ValueExpression;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.io.BufferedReader;

public final class PopulationManager {
    private static Map<String, PopulationContainer> populations = new ConcurrentHashMap<>();

    public static void loadPopulation(String name, String filename) {
        try {
            PopulationManager.populations.put(name, PopulationManager.loadFromCSV(filename));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addPopulation(String name, PopulationContainer container) {
        if (PopulationManager.populations.containsKey(name)) {
            return;
        }
        PopulationManager.populations.put(name, container);
    }

    public static PopulationContainer getPopulation(String name) {
        return PopulationManager.populations.get(name);
    }

    public static List<String> getStoredPopulations() {
        List<String> names = new ArrayList<>();
        for (String key : PopulationManager.populations.keySet()) {
            names.add(key);
        }
        return names;
    }

    public static IDemandView createPopulationView(String name, Envelope envelope, int[] indizes) {
        PopulationContainer population = PopulationManager.getPopulation(name);
        return new PopulationContainerView(population, envelope, indizes, null);
    }

    public static IDemandView createPopulationView(String name, Envelope envelope, int[] indizes, float[] factors) {
        PopulationContainer population = PopulationManager.getPopulation(name);
        return new PopulationContainerView(population, envelope, indizes, factors);
    }

    public static IDemandView createPopulationView(String name, Geometry area, int[] indizes) {
        PopulationContainer population = PopulationManager.getPopulation(name);
        return new PopulationContainerView(population, area, indizes, null);
    }

    public static IDemandView createPopulationView(String name, Geometry area, int[] indizes, float[] factors) {
        PopulationContainer population = PopulationManager.getPopulation(name);
        return new PopulationContainerView(population, area, indizes, factors);
    }

    public static PopulationContainer loadFromCSV(String filename) throws Exception {
        // set dvan population data keys
        String[] population_keys = { "EW_GESAMT", "STND00_09", "STND10_19", "STND20_39", "STND40_59", "STND60_79",
                "STND80X", "KITA_SCHUL", "KITA_SC_01", "KITA_SC_02", "KITA_SC_03", "KITA_SC_04", "KITA_SC_05",
                "KITA_SC_06" };
        HashMap<String, Integer> key_indices = new HashMap<>();
        key_indices.put("EW_GESAMT", -1);
        key_indices.put("GEOM", -1);
        key_indices.put("GEOM_UTM", -1);
        key_indices.put("STND00_09", -1);
        key_indices.put("STND10_19", -1);
        key_indices.put("STND20_39", -1);
        key_indices.put("STND40_59", -1);
        key_indices.put("STND60_79", -1);
        key_indices.put("STND80X", -1);
        key_indices.put("KITA_SCHUL", -1);
        key_indices.put("KITA_SC_01", -1);
        key_indices.put("KITA_SC_02", -1);
        key_indices.put("KITA_SC_03", -1);
        key_indices.put("KITA_SC_04", -1);
        key_indices.put("KITA_SC_05", -1);
        key_indices.put("KITA_SC_06", -1);

        FileReader file = new FileReader(filename);
        BufferedReader reader = new BufferedReader(file);

        String del = ";";
        String line = reader.readLine();
        String[] tokens = line.split(del);

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (key_indices.containsKey(token)) {
                key_indices.put(token, i);
            }
        }

        // create population container
        HashMap<Integer, String> index_to_key = new HashMap<>();
        for (String key : population_keys) {
            int index = key_indices.get(key);
            index_to_key.put(index, key);
        }
        PopulationContainer population = new PopulationContainer(10000, index_to_key);

        // read population from file
        WKBReader geom_reader = new WKBReader();
        while ((line = reader.readLine()) != null) {
            tokens = line.split(del);
            int[] population_values = new int[population_keys.length];
            for (int i = 0; i < population_keys.length; i++) {
                String key = population_keys[i];
                int index = key_indices.get(key);
                int count = (int) Double.parseDouble(tokens[index].replace(",", "."));
                population_values[i] = count;
            }
            int index_geom = key_indices.get("GEOM");
            int index_geom_utm = key_indices.get("GEOM_UTM");
            Point point = (Point) geom_reader.read(WKBReader.hexToBytes(tokens[index_geom]));
            Point utm_point = (Point) geom_reader.read(WKBReader.hexToBytes(tokens[index_geom_utm]));
            population.addPopulationPoint(point.getCoordinate(), utm_point.getCoordinate(), population_values);
        }
        reader.close();

        return population;
    }
}
