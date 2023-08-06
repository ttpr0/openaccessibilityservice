package org.tud.oas.api.responses;

/// <summary>
/// Error response containing the request endpoint and a error message.
/// </summary>
public class ErrorResponse {
    /// <summary>
    /// The request endpoint.
    /// </summary>
    /// <example>api/endpoint/action</example>
    public String request;

    /// <summary>
    /// The error message.
    /// </summary>
    /// <example>invalid request</example>
    public String error;

    public ErrorResponse(String request, String message) {
        this.request = request;
        this.error = message;
    }
}
