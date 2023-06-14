using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;
using NetTopologySuite.Index.KdTree;

namespace DVAN.Routing.ORS
{
    public class IsoRaster
    {
        public String type { get; set; }

        public double precession { get; set; }

        public String crs { get; set; }

        public double[] extend { get; set; }

        public int[] size { get; set; }

        public double[][] envelope { get; set; }

        public List<GridFeature> features { get; set; }

        public IsoRaster() { }

        private KdTree<object> index;

        public String getType()
        {
            return type;
        }

        public double getPrecession()
        {
            return precession;
        }

        public String getCrs()
        {
            return crs;
        }

        public double[] getExtend()
        {
            return extend;
        }

        public int[] getSize()
        {
            return size;
        }

        public double[][] getEnvelope()
        {
            return this.envelope;
        }

        public void constructIndex()
        {
            this.index = new KdTree<object>();
            foreach (GridFeature feature in this.features) {
                this.index.Insert(new Coordinate(feature.x, feature.y), feature.value);
            }
        }

        Envelope _envelope = new Envelope();

        public Dictionary<int, int>? getValueAtCoordinate(Coordinate coord)
        {
            this._envelope.Init(coord.X - 1000, coord.X + 1000, coord.Y - 1000, coord.Y + 1000);
            IList<KdNode<object>> nodes = this.index.Query(this._envelope);
            if (nodes.Count == 0) {
                return null;
            }
            var value = (Dictionary<int, int>)nodes[0].Data;
            return value;
        }

        public IsoRasterAccessor? getAccessor(Coordinate coord)
        {
            this._envelope.Init(coord.X - 1000, coord.X + 1000, coord.Y - 1000, coord.Y + 1000);
            IList<KdNode<object>> nodes = this.index.Query(this._envelope);
            if (nodes.Count == 0) {
                return null;
            }
            return new IsoRasterAccessor(coord, nodes);
        }
    }

    public class IsoRasterAccessor
    {
        float[] factors;

        Dictionary<int, int>[] range_dicts;

        HashSet<int> facilities;

        public IsoRasterAccessor(Coordinate coord, IList<KdNode<object>> nodes)
        {
            this.range_dicts = new Dictionary<int, int>[nodes.Count];
            this.factors = new float[nodes.Count];
            this.facilities = new HashSet<int>();

            for (int i = 0; i < nodes.Count; i++) {
                var node = nodes[i];
                var value = (Dictionary<int, int>)node.Data;
                var node_coord = node.Coordinate;
                this.factors[i] = (float)coord.Distance(node_coord);
                this.range_dicts[i] = value;
                foreach (var f in value.Keys) {
                    this.facilities.Add(f);
                }
            }
        }

        public HashSet<int> getFacilities()
        {
            return this.facilities;
        }

        public float getRange(int facility)
        {
            float range = 0;
            float factor_sum = 0;
            for (int i = 0; i < this.factors.Length; i++) {
                var dict = this.range_dicts[i];
                var factor = this.factors[i];
                if (dict.ContainsKey(facility)) {
                    factor_sum += factor;
                    range += dict[facility] * factor;
                }
            }
            return range / factor_sum;
        }
    }

    public class GridFeature
    {
        public float x { get; set; }
        public float y { get; set; }
        public Dictionary<int, int> value { get; set; }

        public GridFeature() { }

        public GridFeature(float x, float y, Dictionary<int, int> value)
        {
            this.x = x;
            this.y = y;
            this.value = value;
        }
    }

    public class GridValue
    {
        public int range;

        public GridValue() { }

        public GridValue(int value)
        {
            this.range = value;
        }
    }
}