package org.tud.oas.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;

public class Util {
    public static String sendPOST(String url, String request_body) throws Exception {
        Builder builder = HttpRequest.newBuilder();
        builder.uri(new URI(url));
        builder.header("Content-Type", "application/json");
        builder.POST(BodyPublishers.ofString(request_body));
        HttpRequest request = builder.build();

        HttpClient client = HttpClient.newHttpClient();
        BodyHandler<String> bodyHandler = BodyHandlers.ofString();
        HttpResponse<String> response = client.send(request, bodyHandler);

        return response.body();
    }
}
