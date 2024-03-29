package org.tud.oas.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AccessResponseParams", description = """
        """)
public class AccessResponseParams {
    // **************************************
    // response parameters
    // **************************************
    @Schema(name = "scale", description = """
            True if response values should be normalized into a range.
            """, example = "false")
    @JsonProperty("scale")
    public Boolean scale;

    @Schema(name = "scale_range", description = """
            Range the response values should be normalized to.
            """, example = "[0, 100]")
    @JsonProperty("scale_range")
    public int[] scale_range;

    @Schema(name = "no_data_value", description = """
            Value no-data locations should be assigned to.
            """, example = "-9999")
    @JsonProperty("no_data_value")
    public Float no_data_value;
}
