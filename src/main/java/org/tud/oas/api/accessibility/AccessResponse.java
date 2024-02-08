package org.tud.oas.api.accessibility;

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

    public AccessResponse(float[] access, IDemandView population, AccessResponseParams params) {
        if (!checkParams(params)) {
            return;
        }
        boolean scale = false;
        int[] scale_range = { 0, 100 };
        float no_data_value = -9999;
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
        }
        this.access = access;
    }

    public static boolean checkParams(AccessResponseParams params) {
        if (params != null) {
            if (params.scale && params.scale_range != null) {
                if (params.scale_range.length != 2) {
                    return false;
                }
                if (params.scale_range[0] >= params.scale_range[1]) {
                    return false;
                }
            }
        }
        return true;
    }
}
