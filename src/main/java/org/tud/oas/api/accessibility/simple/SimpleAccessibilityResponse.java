package org.tud.oas.api.accessibility.simple;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SimpleAccessibilityResponse", description = """
        Simple Accessibility response.
        """)
public class SimpleAccessibilityResponse {
    @Schema(name = "access", description = """
            Simple-accessibility values.
            """)
    public SimpleValue[] access;

    public SimpleAccessibilityResponse(SimpleValue[] access) {
        this.access = access;
    }
}

@Schema(name = "SimpleValue", description = """
        Simple value.
        """)
class SimpleValue {
    @Schema(name = "first", description = """
            Range to closest facility.
            """, example = "123")
    public int first;

    @Schema(name = "second", description = """
            Range to second closest facility.
            """, example = "235")
    public int second;

    @Schema(name = "third", description = """
            Range to third closest facility.
            """, example = "412")
    public int third;

    public SimpleValue(int first, int second, int third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}
