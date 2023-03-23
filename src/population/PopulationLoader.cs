using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using NetTopologySuite.Geometries;
using NetTopologySuite.IO;

namespace DVAN.Population
{
    public sealed class PopulationLoader {
        public static PopulationContainer loadFromCSV(string filename) {
            CultureInfo.DefaultThreadCurrentCulture = CultureInfo.InvariantCulture;
            var content = File.ReadAllLines(filename);

            var del = ";";
            string line = content[0];
            string[] tokens = line.Split(del);

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
            for (int i=0; i < tokens.Length; i++) {
                String token = tokens[i];
                if (token.Equals("EW_GESAMT")) {
                    index_ew_gesamt = i;
                }
                if (token.Equals("GEOM")) {
                    index_geom = i;
                }
                if (token.Equals("GEOM_UTM")) {
                    index_geom_utm = i;
                }
                if (token.Equals("STND00_09")) {
                    index_stnd00_09 = i;
                }
                if (token.Equals("STND10_19")) {
                    index_stnd10_19 = i;
                }
                if (token.Equals("STND20_39")) {
                    index_stnd20_39 = i;
                }
                if (token.Equals("STND40_59")) {
                    index_stnd40_59 = i;
                }
                if (token.Equals("STND60_79")) {
                    index_stnd60_79 = i;
                }
                if (token.Equals("STND80X")) {
                    index_stnd80x = i;
                }
                if (token.Equals("KITA_SCHUL")) {
                    index_kisc00_02 = i;
                }
                if (token.Equals("KITA_SC_01")) {
                    index_kisc03_05 = i;
                }
                if (token.Equals("KITA_SC_02")) {
                    index_kisc06_09 = i;
                }
                if (token.Equals("KITA_SC_03")) {
                    index_kisc10_14 = i;
                }
                if (token.Equals("KITA_SC_04")) {
                    index_kisc15_17 = i;
                }
                if (token.Equals("KITA_SC_05")) {
                    index_kisc18_19 = i;
                }
                if (token.Equals("KITA_SC_06")) {
                    index_kisc20x = i;
                }
            }
            PopulationContainer population = new PopulationContainer(10000);
            WKBReader geom_reader = new WKBReader();

            for (int i=1; i<content.Length; i++) {
                line = content[i];
                tokens = line.Split(del);
                int ew_gesamt = (int)Convert.ToDouble(tokens[index_ew_gesamt].Replace(",", "."));
                int stnd00_09 = (int)Convert.ToDouble(tokens[index_stnd00_09].Replace(",", "."));
                int stnd10_19 = (int)Convert.ToDouble(tokens[index_stnd10_19].Replace(",", "."));
                int stnd20_39 = (int)Convert.ToDouble(tokens[index_stnd20_39].Replace(",", "."));
                int stnd40_59 = (int)Convert.ToDouble(tokens[index_stnd40_59].Replace(",", "."));
                int stnd60_79 = (int)Convert.ToDouble(tokens[index_stnd60_79].Replace(",", "."));
                int stnd80x = (int)Convert.ToDouble(tokens[index_stnd80x].Replace(",", "."));
                int kisc00_02 = (int)Convert.ToDouble(tokens[index_kisc00_02].Replace(",", "."));
                int kisc03_05 = (int)Convert.ToDouble(tokens[index_kisc03_05].Replace(",", "."));
                int kisc06_09 = (int)Convert.ToDouble(tokens[index_kisc06_09].Replace(",", "."));
                int kisc10_14 = (int)Convert.ToDouble(tokens[index_kisc10_14].Replace(",", "."));
                int kisc15_17 = (int)Convert.ToDouble(tokens[index_kisc15_17].Replace(",", "."));
                int kisc18_19 = (int)Convert.ToDouble(tokens[index_kisc18_19].Replace(",", "."));
                int kisc20x = (int)Convert.ToDouble(tokens[index_kisc20x].Replace(",", "."));
                int[] standard_population = new int[] {stnd00_09, stnd10_19, stnd20_39, (int)(stnd40_59/2), (int)(stnd40_59/2), stnd60_79, stnd80x};
                int[] kita_schul_population = new int[] {kisc00_02, kisc03_05, kisc06_09, kisc10_14, kisc15_17, kisc18_19, kisc20x};
                PopulationAttributes attributes = new PopulationAttributes(ew_gesamt, standard_population, kita_schul_population);
                Point point = (Point)geom_reader.Read(WKBReader.HexToBytes(tokens[index_geom]));
                Point utm_point = (Point)geom_reader.Read(WKBReader.HexToBytes(tokens[index_geom_utm]));
                population.addPopulationPoint(point.Coordinate, utm_point.Coordinate, attributes);
            }
            return population;
        }
    }
}
