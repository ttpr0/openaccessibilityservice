package org.tud.oas.population;

import java.io.FileReader;

import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.geom.Point;

import java.io.BufferedReader;

public final class PopulationLoader {
    public static Population loadFromCSV(String filename) throws Exception {
        FileReader file = new FileReader(filename);
		BufferedReader reader = new BufferedReader(file);

		String del = ";";
		String line = reader.readLine();
		String[] tokens = line.split(del);
        int index_ew = -1;
        int index_geom = -1;
        int index_geom_utm = -1;
        for (int i=0; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.equals("EW_GESAMT")) {
                index_ew = i;
            }
            if (token.equals("GEOM")) {
                index_geom = i;
            }
            if (token.equals("GEOM_UTM")) {
                index_geom_utm = i;
            }
        }
        System.out.println(index_ew + "," + index_geom + "," + index_geom_utm);
        Population population = new Population(10000);
        WKBReader geom_reader = new WKBReader();

		while ((line = reader.readLine()) != null)
		{
			tokens = line.split(del);
            int count = (int)Double.parseDouble(tokens[index_ew].replace(",", "."));
            PopulationAttributes attributes = new PopulationAttributes(count);
            Point point = (Point)geom_reader.read(WKBReader.hexToBytes(tokens[index_geom]));
            Point utm_point = (Point)geom_reader.read(WKBReader.hexToBytes(tokens[index_geom_utm]));
            population.addPopulationPoint(point, (float)utm_point.getX(), (float)utm_point.getY(), attributes);
		}

		reader.close();

        return population;
    }
}
