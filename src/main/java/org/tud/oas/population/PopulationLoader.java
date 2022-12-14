package org.tud.oas.population;

import java.io.FileReader;

import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.geom.Point;

import java.io.BufferedReader;

public final class PopulationLoader {
    public static Population loadFromCSV(String filename) throws Exception {
        FileReader file = new FileReader(filename);
		BufferedReader reader = new BufferedReader(file);

		String del = ";";
		String line = reader.readLine();
		String[] tokens = line.split(del);
        Population population = new Population(10000);
        WKBReader geom_reader = new WKBReader();

		while ((line = reader.readLine()) != null)
		{
			tokens = line.split(del);
            int count = (int)Double.parseDouble(tokens[23].replace(",", "."));
            PopulationAttributes attributes = new PopulationAttributes(count);
            Point point = (Point)geom_reader.read(WKBReader.hexToBytes(tokens[32]));
            population.addPopulationPoint(point, attributes);
		}

		reader.close();

        return population;
    }
}
