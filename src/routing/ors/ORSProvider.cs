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
using DVAN.Population;
using NetTopologySuite.Algorithm.Locate;
using NetTopologySuite.Simplify;

namespace DVAN.Routing.ORS
{
    public class ORSProvider : IRoutingProvider
    {
        private string url;

        private string profile = "driving-car";
        private string range_type = "time";
        private string location_type = "destination";
        private float isochrone_smoothing = (float)5.0;
        private Dictionary<string, object>? options;

        public ORSProvider(string url)
        {
            this.url = url;
        }

        public void setProfile(string profile)
        {
            this.profile = profile;
        }

        public void setRangeType(string range_type)
        {
            this.range_type = range_type;
        }

        public void setOption(string name, object value)
        {
            switch (name) {
                case "location_type":
                    this.location_type = (string)value;
                    break;
                case "isochrone_smoothing":
                    this.isochrone_smoothing = (float)value;
                    break;
                default:
                    if (this.options == null) {
                        this.options = new Dictionary<string, object>();
                    }
                    this.options[name] = value;
                    break;
            }
        }

        public Task<ITDMatrix?> requestTDMatrix(IPopulationView population, double[][] facilities, List<double> ranges, string mode)
        {
            switch (mode) {
                case "isochrones":
                    return this.requestMatrixIsochrones(population, facilities, ranges);
                case "matrix":
                    return this.requestMatrixMatrix(population, facilities, ranges);
                case "isoraster":
                    return this.requestMatrixIsoraster(population, facilities, ranges);
                default:
                    return this.requestMatrixMatrix(population, facilities, ranges);
            }
        }

        private async Task<ITDMatrix?> requestMatrixIsochrones(IPopulationView population, double[][] facilities, List<double> ranges)
        {
            var matrix = new float[facilities.Length, population.pointCount()];
            for (int i = 0; i < facilities.Length; i++) {
                for (int j = 0; j < population.pointCount(); j++) {
                    matrix[i, j] = 9999;
                }
            }

            var collection = this.requestIsochronesStream(facilities, ranges);
            for (int f = 0; f < facilities.Length; f++) {
                var isochrones = await collection.ReceiveAsync();
                if (isochrones == null) {
                    continue;
                }
                int facility_index = isochrones.getID();

                var visited = new HashSet<int>(10000);
                for (int i = 0; i < isochrones.getIsochronesCount(); i++) {
                    var isochrone = isochrones.getIsochrone(i);
                    double range = isochrone.getValue();

                    Geometry iso = isochrone.getGeometry();
                    Envelope env = iso.EnvelopeInternal;

                    List<int> points = population.getPointsInEnvelop(env);
                    foreach (int index in points) {
                        if (visited.Contains(index)) {
                            continue;
                        }
                        Coordinate p = population.getCoordinate(index);
                        var location = SimplePointInAreaLocator.Locate(p, iso);
                        if (location == Location.Interior) {
                            matrix[facility_index, index] = (float)range;
                            visited.Add(index);
                        }
                    }
                }
            }

            return new TDMatrix2(matrix);
        }

        private async Task<ITDMatrix?> requestMatrixMatrix(IPopulationView population, double[][] facilities, List<double> ranges)
        {
            float max_range = (float)ranges[^1];

            var point_count = population.pointCount();
            double[][] destinations = new double[point_count][];
            for (int i = 0; i < point_count; i++) {
                var index = i;
                Coordinate p = population.getCoordinate(index);
                destinations[i] = new double[] { p.X, p.Y };
            }
            var matrix = await this.requestMatrix(facilities, destinations);
            if (matrix == null || matrix.durations == null) {
                return null;
            }

            return new TDMatrix(matrix.durations);
        }

        private async Task<ITDMatrix?> requestMatrixIsoraster(IPopulationView population, double[][] facilities, List<double> ranges)
        {
            var matrix = new float[facilities.Length, population.pointCount()];
            for (int i = 0; i < facilities.Length; i++) {
                for (int j = 0; j < population.pointCount(); j++) {
                    matrix[i, j] = 9999;
                }
            }

            float max_range = (float)ranges[^1];
            var isoraster = await this.requestIsoRaster(facilities, max_range);
            if (isoraster == null) {
                return null;
            }

            double[][] extend = isoraster.getEnvelope();
            var env = new Envelope(extend[0][0], extend[3][0], extend[2][1], extend[1][1]);
            var points = population.getPointsInEnvelop(env);
            foreach (int index in points) {
                Coordinate p = population.getCoordinate(index, "EPSG:25832");
                var accessor = isoraster.getAccessor(p);
                if (accessor != null) {
                    foreach (var f in accessor.getFacilities()) {
                        float range = accessor.getRange(f);

                        matrix[f, index] = range;
                    }
                }
            }

            return new TDMatrix2(matrix);
        }

        public async Task<INNTable?> requestNearest(IPopulationView population, double[][] facilities, List<double> ranges, string mode)
        {
            var table = new (int, float)[population.pointCount()];
            for (int j = 0; j < population.pointCount(); j++) {
                table[j] = (-1, (float)9999);
            }

            var polygons = new Dictionary<double, Geometry>(ranges.Count);
            var collection = this.requestIsochronesStream(facilities, ranges);
            for (int j = 0; j < facilities.Length; j++) {
                var isochrones = await collection.ReceiveAsync();
                if (isochrones == null) {
                    continue;
                }

                for (int i = 0; i < isochrones.getIsochronesCount(); i++) {
                    Isochrone isochrone = isochrones.getIsochrone(i);
                    double range = isochrone.getValue();

                    if (!polygons.ContainsKey(range)) {
                        polygons[range] = isochrone.getGeometry();
                    }
                    else {
                        Geometry geometry = polygons[range];
                        Geometry union = geometry.Union(isochrone.getGeometry());
                        Geometry geom = new PolygonHullSimplifier(union, false).GetResult();
                        polygons[range] = geom;
                    }
                }
            }

            var visited = new HashSet<int>(10000);
            for (int i = 0; i < ranges.Count; i++) {
                double range = ranges[i];
                Geometry iso = polygons[range];

                Envelope env = iso.EnvelopeInternal;
                List<int> points = population.getPointsInEnvelop(env);

                Geometry geom = new PolygonHullSimplifier(iso, false).GetResult();

                foreach (int index in points) {
                    if (visited.Contains(index)) {
                        continue;
                    }
                    Coordinate p = population.getCoordinate(index);
                    var location = SimplePointInAreaLocator.Locate(p, geom);
                    if (location == Location.Interior) {
                        visited.Add(index);
                        table[index] = (-1, (float)range);
                    }
                }
            }

            return new NNTable(table);
        }

        public async Task<IKNNTable?> requestKNearest(IPopulationView population, double[][] facilities, List<double> ranges, int n, string mode)
        {
            var table = new (int, float)[population.pointCount(), n];
            for (int j = 0; j < population.pointCount(); j++) {
                for (int i = 0; i < n; i++) {
                    table[j, i] = (-1, (float)9999);
                }
            }

            var collection = this.requestIsochronesStream(facilities, ranges);
            for (int j = 0; j < facilities.Length; j++) {
                var isochrones = await collection.ReceiveAsync();
                if (isochrones == null) {
                    continue;
                }

                int facility_index = isochrones.getID();
                var visited = new HashSet<int>(10000);

                for (int i = 0; i < isochrones.getIsochronesCount(); i++) {
                    Isochrone isochrone = isochrones.getIsochrone(i);
                    double range = isochrone.getValue();
                    Geometry iso;
                    Geometry outer = isochrone.getGeometry();
                    if (i == 0) {
                        iso = outer;
                    }
                    else {
                        Geometry inner = isochrones.getIsochrone(i - 1).getGeometry();
                        iso = outer.Difference(inner);
                    }

                    Envelope env = iso.EnvelopeInternal;
                    List<int> points = population.getPointsInEnvelop(env);

                    foreach (int index in points) {
                        if (visited.Contains(index)) {
                            continue;
                        }
                        Coordinate p = population.getCoordinate(index);
                        var location = SimplePointInAreaLocator.Locate(p, iso);
                        if (location == Location.Interior) {
                            visited.Add(index);
                            // insert new range while keeping array-dimension sorted
                            var (_, last_range) = table[index, n - 1];
                            if (last_range > range || last_range == 9999) {
                                table[index, n - 1] = (facility_index, (float)range);
                                for (int k = n - 2; k > 0; k++) {
                                    var curr = table[index, k];
                                    var prev = table[index, k + 1];
                                    if (curr.Item2 > prev.Item2 || curr.Item2 == 9999) {
                                        table[index, k] = prev;
                                    }
                                    else {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return new KNNTable(table);
        }

        public async Task<ICatchment?> requestCatchment(IPopulationView population, double[][] facilities, double range, string mode)
        {
            var accessibilities = new List<int>[population.pointCount()];

            var collection = this.requestIsochronesStream(facilities, new List<double> { range });
            for (int f = 0; f < facilities.Length; f++) {
                var isochrones = await collection.ReceiveAsync();
                if (isochrones == null) {
                    continue;
                }
                int facility_index = isochrones.getID();

                Isochrone isochrone = isochrones.getIsochrone(0);
                Geometry iso = isochrone.getGeometry();

                Envelope env = iso.EnvelopeInternal;
                List<int> points = population.getPointsInEnvelop(env);
                foreach (int index in points) {
                    Coordinate p = population.getCoordinate(index);
                    var location = SimplePointInAreaLocator.Locate(p, iso);
                    if (location == Location.Interior) {
                        List<int> access;
                        if (accessibilities[index] == null) {
                            access = new List<int>();
                            accessibilities[index] = access;
                        }
                        else {
                            access = accessibilities[index];
                        }
                        accessibilities[index].Add(facility_index);
                    }
                }
            }

            return new Catchment(accessibilities);
        }

        private async Task<List<IsochroneCollection>> requestIsochrones(double[][] locations, List<double> ranges)
        {
            var request = new Dictionary<string, object> {
                ["locations"] = locations,
                ["location_type"] = this.location_type,
                ["range"] = ranges,
                ["range_type"] = this.range_type,
                ["units"] = "m",
                ["smoothing"] = this.isochrone_smoothing,
            };

            try {
                var jsonRequest = JsonSerializer.Serialize(request);
                var httpClient = new HttpClient();
                var content = new StringContent(jsonRequest, Encoding.UTF8, "application/json");
                var response = await httpClient.PostAsync(this.url + "/v2/isochrones/" + this.profile + "/geojson", content);

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

        private ISourceBlock<IsochroneCollection?> requestIsochronesStream(double[][] locations, List<double> ranges)
        {
            var buffer = new BufferBlock<IsochroneCollection>();
            for (int i = 0; i < locations.Length; i++) {
                int index = i;
                Task.Run(async () => {
                    var request = new Dictionary<string, object> {
                        ["location_type"] = this.location_type,
                        ["range"] = ranges,
                        ["range_type"] = this.range_type,
                        ["units"] = "m",
                        ["smoothing"] = this.isochrone_smoothing,
                    };
                    double[][] locs = new double[1][];
                    locs[0] = new double[] { locations[index][0], locations[index][1] };
                    request["locations"] = locs;

                    try {
                        var jsonRequest = JsonSerializer.Serialize(request);
                        var httpClient = new HttpClient();
                        var content = new StringContent(jsonRequest, Encoding.UTF8, "application/json");
                        var response = await httpClient.PostAsync(this.url + "/v2/isochrones/" + this.profile + "/geojson", content);

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

        private async Task<IsoRaster?> requestIsoRaster(double[][] locations, double max_range)
        {
            double[] ranges = { max_range };
            var request = new Dictionary<string, object> {
                ["locations"] = locations,
                ["location_type"] = this.location_type,
                ["range"] = ranges,
                ["range_type"] = this.range_type,
                ["units"] = "m",
                ["consumer_type"] = "node_based",
                ["crs"] = "25832",
                ["precession"] = 1000,
            };

            try {
                var jsonRequest = JsonSerializer.Serialize(request);
                var httpClient = new HttpClient();
                var content = new StringContent(jsonRequest, Encoding.UTF8, "application/json");
                var response = await httpClient.PostAsync(this.url + "/v2/isoraster/" + this.profile, content);
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

        private ISourceBlock<IsoRaster?> requestIsoRasterStream(double[][] locations, double max_range)
        {
            var buffer = new BufferBlock<IsoRaster?>();
            for (int i = 0; i < locations.Length; i++) {
                int index = i;
                Task.Run(async () => {
                    double[] ranges = { max_range };
                    var request = new Dictionary<string, object> {
                        ["location_type"] = this.location_type,
                        ["range"] = ranges,
                        ["range_type"] = this.range_type,
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
                        var response = await httpClient.PostAsync(this.url + "/v2/isoraster/" + this.profile, content);
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

        private async Task<Matrix?> requestMatrix(double[][] sources, double[][] destinations)
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
                ["metrics"] = this.range_type == "time" ? "duration" : this.range_type,
            };

            try {

                var jsonRequest = JsonSerializer.Serialize(request);
                var httpClient = new HttpClient();
                var content = new StringContent(jsonRequest, Encoding.UTF8, "application/json");
                var response = await httpClient.PostAsync(this.url + "/v2/matrix/" + this.profile, content);
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