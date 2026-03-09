package io.github.koyan9.tvremote.integration;

import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class HttpGatewayInfraredSessionClient implements GatewayInfraredSessionClient {

    private final HttpClient httpClient;

    public HttpGatewayInfraredSessionClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    @Override
    public GatewayInfraredSessionResult sendCommand(GatewayInfraredCommandRequest request) {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(request.endpoint())
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "application/json")
                    .header("X-Hub-Id", request.hubId())
                    .header("Authorization", "Bearer " + request.authToken())
                    .POST(HttpRequest.BodyPublishers.ofString(request.payload()))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Infrared gateway returned HTTP " + response.statusCode());
            }

            return new GatewayInfraredSessionResult(
                    request.endpoint(),
                    response.statusCode(),
                    "Sent an infrared gateway HTTP request using profile " + request.profileKey() + "."
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Infrared gateway request failed: " + exception.getMessage(), exception);
        }
    }
}
