using System;
using System.Collections.Generic;
using System.Web;
using System.Text.Json;
using System.Text.Json.Nodes;
using System.Net.Http;
using System.Text;
using NetTopologySuite.Geometries;
using System.Threading.Tasks;
using System.Threading.Tasks.Dataflow;

namespace DVAN.Routing
{
    public class ORSProvider : IRoutingProvider
    {
        private string url;

        public ORSProvider(String url)
        {
            this.url = url;
        }

        async public Task<List<IsochroneCollection>> requestIsochrones(double[][] locations, List<double> ranges)
        {
            var request = new Dictionary<string, object> {
                ["locations"] = locations,
                ["location_type"] = "destination",
                ["range"] = ranges,
                ["range_type"] = "time",
                ["units"] = "m",
                ["smoothing"] = 5.0
            };

            try {
                var jsonRequest = JsonSerializer.Serialize(request);
                var httpClient = new HttpClient();
                var content = new StringContent(jsonRequest, Encoding.UTF8, "application/json");
                var response = await httpClient.PostAsync(this.url + "/v2/isochrones/driving-car/geojson", content);

                var isoColls = new List<IsochroneCollection>(locations.Length);

                var jsonOptions = new JsonSerializerOptions {
                    PropertyNameCaseInsensitive = true
                };

                using var stream = await response.Content.ReadAsStreamAsync();
                using var doc = await JsonDocument.ParseAsync(stream);

                var features = doc.RootElement.GetProperty("features");
                var isochrones = new List<Isochrone>();
                Envelope envelope = null;
                Coordinate center = new Coordinate(0, 0);
                var geomFactory = new GeometryFactory();
                foreach (var feature in features.EnumerateArray()) {
                    var coords = feature.GetProperty("geometry").GetProperty("coordinates");
                    var polygon = JsonSerializer.Deserialize<double[][][]>(coords.GetRawText())[0];
                    var coordinates = new Coordinate[polygon.Length];
                    for (var i = 0; i < polygon.Length; i++) {
                        coordinates[i] = new Coordinate(polygon[i][0], polygon[i][1]);
                    }

                    var geometry = geomFactory.CreatePolygon(coordinates);
                    var isochrone = new Isochrone(geometry, feature.GetProperty("properties").GetProperty("value").GetDouble());
                    isochrones.Add(isochrone);
                }

                var isoColl = new IsochroneCollection(0, envelope, isochrones, center);
                isoColls.Add(isoColl);

                return isoColls;
            }
            catch (Exception) {
                return null;
            }
        }

        public ISourceBlock<IsochroneCollection?> requestIsochronesStream(double[][] locations, List<double> ranges)
        {
            var buffer = new BufferBlock<IsochroneCollection>();
            for (int i = 0; i < locations.Length; i++) {
                int index = i;
                Task.Run(async () => {
                    var request = new Dictionary<string, object> {
                        ["location_type"] = "destination",
                        ["range"] = ranges,
                        ["range_type"] = "time",
                        ["units"] = "m",
                        ["smoothing"] = 5.0
                    };
                    double[][] locs = new double[1][];
                    locs[0] = new double[] { locations[index][0], locations[index][1] };
                    request["locations"] = locs;

                    try {
                        var jsonRequest = JsonSerializer.Serialize(request);
                        var httpClient = new HttpClient();
                        var content = new StringContent(jsonRequest, Encoding.UTF8, "application/json");
                        var response = await httpClient.PostAsync(this.url + "/v2/isochrones/driving-car/geojson", content);

                        var isoColls = new List<IsochroneCollection>(locations.Length);

                        using var stream = await response.Content.ReadAsStreamAsync();
                        using var doc = await JsonDocument.ParseAsync(stream);

                        var features = doc.RootElement.GetProperty("features");
                        var isochrones = new List<Isochrone>();
                        Envelope envelope = null;
                        Coordinate center = new Coordinate(0, 0);
                        var geomFactory = new GeometryFactory();
                        foreach (var feature in features.EnumerateArray()) {
                            var coords = feature.GetProperty("geometry").GetProperty("coordinates");
                            var polygon = JsonSerializer.Deserialize<double[][][]>(coords.GetRawText())[0];
                            var coordinates = new Coordinate[polygon.Length];
                            for (var i = 0; i < polygon.Length; i++) {
                                coordinates[i] = new Coordinate(polygon[i][0], polygon[i][1]);
                            }

                            var geometry = geomFactory.CreatePolygon(coordinates);
                            var isochrone = new Isochrone(geometry, feature.GetProperty("properties").GetProperty("value").GetDouble());
                            isochrones.Add(isochrone);
                        }

                        await buffer.SendAsync(new IsochroneCollection(index, envelope, isochrones, center));
                    }
                    catch (Exception e) {
                        await buffer.SendAsync(null);
                    }
                });
            }
            return buffer;
        }

        public async Task<IsoRaster?> requestIsoRaster(Double[][] locations, double max_range)
        {
            double[] ranges = { max_range };
            var request = new Dictionary<string, object> {
                ["locations"] = locations,
                ["location_type"] = "destination",
                ["range"] = ranges,
                ["range_type"] = "time",
                ["units"] = "m",
                ["consumer_type"] = "node_based",
                ["crs"] = "25832",
                ["precession"] = 1000,
            };

            try {
                var jsonRequest = JsonSerializer.Serialize(request);
                var httpClient = new HttpClient();
                var content = new StringContent(jsonRequest, Encoding.UTF8, "application/json");
                var response = await httpClient.PostAsync(this.url + "/v2/isoraster/driving-car", content);
                using var stream = await response.Content.ReadAsStreamAsync();
                var raster = JsonSerializer.Deserialize<IsoRaster>(stream);
                if (raster == null) {
                    return null;
                }
                raster.constructIndex();

                return raster;
            }
            catch (Exception e) {
                return null;
            }
        }

        public ISourceBlock<IsoRaster?> requestIsoRasterStream(Double[][] locations, double max_range)
        {
            var buffer = new BufferBlock<IsoRaster?>();
            for (int i = 0; i < locations.Length; i++) {
                int index = i;
                Task.Run(async () => {
                    double[] ranges = { max_range };
                    var request = new Dictionary<string, object> {
                        ["location_type"] = "destination",
                        ["range"] = ranges,
                        ["range_type"] = "time",
                        ["units"] = "m",
                        ["consumer_type"] = "node_based",
                        ["crs"] = "25832",
                        ["precession"] = 1000,
                    };

                    try {
                        double[][] locs = new double[1][];
                        locs[0] = new double[] { locations[index][0], locations[index][1] };
                        request["locations"] = locs;

                        var jsonRequest = JsonSerializer.Serialize(request);
                        var httpClient = new HttpClient();
                        var content = new StringContent(jsonRequest, Encoding.UTF8, "application/json");
                        var response = await httpClient.PostAsync(this.url + "/v2/isoraster/driving-car", content);
                        using var stream = await response.Content.ReadAsStreamAsync();
                        var raster = JsonSerializer.Deserialize<IsoRaster>(stream);
                        raster.constructIndex();

                        await buffer.SendAsync(raster);
                    }
                    catch (Exception e) {
                        await buffer.SendAsync(null);
                    }
                });
            }
            return buffer;
        }

        public async Task<Matrix?> requestMatrix(double[][] sources, double[][] destinations)
        {
            double[][] locations = new double[sources.Length + destinations.Length][];
            int[] source = new int[sources.Length];
            int[] destination = new int[destinations.Length];
            int c = 0;
            for (int i = 0; i < sources.Length; i++) {
                source[i] = c;
                locations[c] = sources[i];
                c += 1;
            }
            for (int i = 0; i < destinations.Length; i++) {
                destination[i] = c;
                locations[c] = destinations[i];
                c += 1;
            }

            var request = new Dictionary<string, object> {
                ["locations"] = locations,
                ["sources"] = source,
                ["destinations"] = destination,
                ["units"] = "m",
            };

            try {

                var jsonRequest = JsonSerializer.Serialize(request);
                var httpClient = new HttpClient();
                var content = new StringContent(jsonRequest, Encoding.UTF8, "application/json");
                var response = await httpClient.PostAsync(this.url + "/v2/matrix/driving-car", content);
                using var stream = await response.Content.ReadAsStreamAsync();
                var matrix = JsonSerializer.Deserialize<Matrix>(stream);

                return matrix;
            }
            catch (Exception e) {
                return null;
            }
        }
    }
}