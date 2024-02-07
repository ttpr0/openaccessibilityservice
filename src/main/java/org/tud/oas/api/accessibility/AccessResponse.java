package org.tud.oas.api.accessibility;

import org.locationtech.jts.geom.Coordinate;
import org.tud.oas.demand.IDemandView;
import org.tud.oas.requests.AccessResponseParams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AccessResponse", description = """
        Access response.
        """)
@JsonInclude(Include.NON_NULL)
public class AccessResponse {
    @Schema(name = "access", description = """
            Accessibility values.
            """, example = "[72.34, 29.98, 99.21]")
    public float[] access;

    @Schema(name = "locations", description = """
            Demand locations.
            """, example = "[[9.53, 51.27], [8.67, 50.98], [10.21, 49.30]]")
    public float[][] locations;

    public AccessResponse(float[] access, IDemandView population, AccessResponseParams params) {
        boolean scale = false;
        int[] scale_range = { 0, 100 };
        float no_data_value = -9999;
        boolean return_locs = true;
        String loc_crs = "EPSG:4326";
        if (params != null) {
            if (params.scale != null) {
                scale = params.scale;
            }
            if (scale && params.scale_range != null) {
                scale_range = params.scale_range;
            }
            if (params.no_data_value != null) {
                no_data_value = params.no_data_value;
            }
            if (params.return_locs != null) {
                return_locs = params.return_locs;
            }
            if (return_locs && params.loc_crs != null) {
                loc_crs = params.loc_crs;
            }
        }

        float max = -1000000000;
        float min = 1000000000;
        for (float w : access) {
            if (w > max) {
                max = w;
            }
            if (w < min) {
                min = w;
            }
        }

        float[][] locs;
        if (return_locs) {
            locs = new float[access.length][];
        } else {
            locs = null;
        }
        for (int i = 0; i < access.length; i++) {
            float accessibility = access[i];
            if (accessibility != 0) {
                if (scale) {
                    accessibility = (accessibility + scale_range[0] - min)
                            * ((scale_range[1] - scale_range[0]) / (max - min));
                } else {
                    accessibility = accessibility;
                }
            } else {
                accessibility = no_data_value;
            }
            access[i] = accessibility;
            if (return_locs) {
                Coordinate loc = population.getCoordinate(i);
                // TODO: Project to loc_crs
                locs[i] = new float[] { (float) loc.x, (float) loc.y };
            }
        }
        this.access = access;
        this.locations = locs;
    }
}
