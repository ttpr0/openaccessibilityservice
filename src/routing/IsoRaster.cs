using System;
using System.Collections.Generic;
using NetTopologySuite.Geometries;
using NetTopologySuite.Index.KdTree;

namespace DVAN.Routing 
{
    public class IsoRaster {
        public String type { get; set; }

        public double precession { get; set; }

        public String crs { get; set; }

        public double[] extend { get; set;}

        public int[] size { get; set; }

        public double[][] envelope { get; set; }

        public List<GridFeature> features { get; set; }

        public IsoRaster() {}

        private KdTree<object> index;

        public String getType() {
            return type;
        }

        public double getPrecession() {
            return precession;
        }

        public String getCrs() {
            return crs;
        }

        public double[] getExtend() {
            return extend;
        }

        public int[] getSize() {
            return size;
        }

        public double[][] getEnvelope() {
            return this.envelope;
        }

        public void constructIndex() {
            this.index = new KdTree<object>(500);
            foreach (GridFeature feature in this.features) {
                this.index.Insert(new Coordinate(feature.x, feature.y), feature.value);
            }
        }

        Envelope _envelope = new Envelope();

        public int getValueAtCoordinate(Coordinate coord) {
            this._envelope.Init(coord.X-500, coord.X+500, coord.Y-500, coord.Y+500);
            IList<KdNode<object>> nodes = this.index.Query(this._envelope);
            if (nodes.Count == 0) {
                return -1;
            }
            GridValue value = (GridValue)nodes[0].Data;
            return value.range;
        }
    }
}