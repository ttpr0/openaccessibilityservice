package org.tud.oas.api.status;

public class HealthResponse {
    public Boolean ok;
    public String status;

    public HealthResponse(boolean ok) {
        this.ok = ok;
        this.status = ok ? "OK" : "ERROR";
    }
}
