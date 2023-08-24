package org.tud.oas.demand.population;

import java.io.FileReader;

import org.locationtech.jts.io.WKBReader;
import org.tud.oas.demand.IDemandView;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.io.BufferedReader;

public final class PopulationLoader {
    private static PopulationContainer population;

    public static void loadPopulation(String filename) {
        try {
            PopulationLoader.population = PopulationLoader.loadFromCSV(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PopulationContainer getPopulation() {
        return PopulationLoader.population;
    }

    public static IDemandView createPopulationView(Envelope envelope) {
        return new PopulationContainerView(population, envelope);
    }

    public static IDemandView createPopulationView(Envelope envelope, String type, int[] indizes) {
        return new PopulationContainerView(population, envelope, type, indizes);
    }

    public static IDemandView createPopulationView(Geometry area) {
        return new PopulationContainerView(population, area);
    }

    public static PopulationContainer loadFromCSV(String filename) throws Exception {
        FileReader file = new FileReader(filename);
        BufferedReader reader = new BufferedReader(file);

        String del = ";";
        String line = reader.readLine();
        String[] tokens = line.split(del);

        // population indices
        int index_ew_gesamt = -1;
        int index_stnd00_09 = -1;
        int index_stnd10_19 = -1;
        int index_stnd20_39 = -1;
        int index_stnd40_59 = -1;
        int index_stnd60_79 = -1;
        int index_stnd80x = -1;
        int index_kisc00_02 = -1;
        int index_kisc03_05 = -1;
        int index_kisc06_09 = -1;
        int index_kisc10_14 = -1;
        int index_kisc15_17 = -1;
        int index_kisc18_19 = -1;
        int index_kisc20x = -1;

        // geom indices
        int index_geom = -1;
        int index_geom_utm = -1;
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.equals("EW_GESAMT")) {
                index_ew_gesamt = i;
            }
            if (token.equals("GEOM")) {
                index_geom = i;
            }
            if (token.equals("GEOM_UTM")) {
                index_geom_utm = i;
            }
            if (token.equals("STND00_09")) {
                index_stnd00_09 = i;
            }
            if (token.equals("STND10_19")) {
                index_stnd10_19 = i;
            }
            if (token.equals("STND20_39")) {
                index_stnd20_39 = i;
            }
            if (token.equals("STND40_59")) {
                index_stnd40_59 = i;
            }
            if (token.equals("STND60_79")) {
                index_stnd60_79 = i;
            }
            if (token.equals("STND80X")) {
                index_stnd80x = i;
            }
            if (token.equals("KITA_SCHUL")) {
                index_kisc00_02 = i;
            }
            if (token.equals("KITA_SC_01")) {
                index_kisc03_05 = i;
            }
            if (token.equals("KITA_SC_02")) {
                index_kisc06_09 = i;
            }
            if (token.equals("KITA_SC_03")) {
                index_kisc10_14 = i;
            }
            if (token.equals("KITA_SC_04")) {
                index_kisc15_17 = i;
            }
            if (token.equals("KITA_SC_05")) {
                index_kisc18_19 = i;
            }
            if (token.equals("KITA_SC_06")) {
                index_kisc20x = i;
            }
        }
        System.out.println(index_ew_gesamt + "," + index_geom + "," + index_geom_utm);
        PopulationContainer population = new PopulationContainer(10000);
        WKBReader geom_reader = new WKBReader();

        while ((line = reader.readLine()) != null) {
            tokens = line.split(del);
            int ew_gesamt = (int) Double.parseDouble(tokens[index_ew_gesamt].replace(",", "."));
            int stnd00_09 = (int) Double.parseDouble(tokens[index_stnd00_09].replace(",", "."));
            int stnd10_19 = (int) Double.parseDouble(tokens[index_stnd10_19].replace(",", "."));
            int stnd20_39 = (int) Double.parseDouble(tokens[index_stnd20_39].replace(",", "."));
            int stnd40_59 = (int) Double.parseDouble(tokens[index_stnd40_59].replace(",", "."));
            int stnd60_79 = (int) Double.parseDouble(tokens[index_stnd60_79].replace(",", "."));
            int stnd80x = (int) Double.parseDouble(tokens[index_stnd80x].replace(",", "."));
            int kisc00_02 = (int) Double.parseDouble(tokens[index_kisc00_02].replace(",", "."));
            int kisc03_05 = (int) Double.parseDouble(tokens[index_kisc03_05].replace(",", "."));
            int kisc06_09 = (int) Double.parseDouble(tokens[index_kisc06_09].replace(",", "."));
            int kisc10_14 = (int) Double.parseDouble(tokens[index_kisc10_14].replace(",", "."));
            int kisc15_17 = (int) Double.parseDouble(tokens[index_kisc15_17].replace(",", "."));
            int kisc18_19 = (int) Double.parseDouble(tokens[index_kisc18_19].replace(",", "."));
            int kisc20x = (int) Double.parseDouble(tokens[index_kisc20x].replace(",", "."));
            int[] standard_population = new int[] { stnd00_09, stnd10_19, stnd20_39, (int) (stnd40_59 / 2),
                    (int) (stnd40_59 / 2), stnd60_79, stnd80x };
            int[] kita_schul_population = new int[] { kisc00_02, kisc03_05, kisc06_09, kisc10_14, kisc15_17, kisc18_19,
                    kisc20x };
            PopulationAttributes attributes = new PopulationAttributes(ew_gesamt, standard_population,
                    kita_schul_population);
            Point point = (Point) geom_reader.read(WKBReader.hexToBytes(tokens[index_geom]));
            Point utm_point = (Point) geom_reader.read(WKBReader.hexToBytes(tokens[index_geom_utm]));
            population.addPopulationPoint(point.getCoordinate(), utm_point.getCoordinate(), attributes);
        }

        reader.close();

        return population;
    }
}
