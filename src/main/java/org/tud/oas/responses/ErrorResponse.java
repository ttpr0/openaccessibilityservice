package org.tud.oas.responses;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ErrorResponse", description = """
        Error response containing the request endpoint and a error message.
        """)
public class ErrorResponse {
    @Schema(name = "request", description = """
            The request endpoint.
            """, example = "api/endpoint/action")
    public String request;

    @Schema(name = "error", description = """
            The error message.
            """, example = "invalid request")
    public String error;

    public ErrorResponse(String request, String message) {
        this.request = request;
        this.error = message;
    }
}
