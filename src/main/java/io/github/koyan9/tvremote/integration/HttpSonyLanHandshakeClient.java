package io.github.koyan9.tvremote.integration;

import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class HttpSonyLanHandshakeClient implements SonyLanHandshakeClient {

    private final HttpClient httpClient;

    public HttpSonyLanHandshakeClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    @Override
    public SonyLanHandshakeResult startHandshake(SonyLanHandshakeRequest request) {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(request.endpoint())
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Sony handshake returned HTTP " + response.statusCode());
            }

            return new SonyLanHandshakeResult(
                    request.endpoint(),
                    null,
                    "Opened a Sony BRAVIA onboarding handshake. Pre-shared key capture is not implemented in the first Sony onboarding version."
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Sony handshake failed: " + exception.getMessage(), exception);
        }
    }
}
