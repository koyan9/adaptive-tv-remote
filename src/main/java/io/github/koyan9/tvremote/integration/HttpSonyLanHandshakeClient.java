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
            HttpRequest.Builder builder = HttpRequest.newBuilder(request.endpoint())
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "application/json");
            if (request.preSharedKey() != null && !request.preSharedKey().isBlank()) {
                builder.header("X-Auth-PSK", request.preSharedKey());
            }
            HttpRequest httpRequest = builder
                    .POST(HttpRequest.BodyPublishers.ofString("""
                            {
                              "id": 1,
                              "method": "getSystemInformation",
                              "version": "1.0",
                              "params": []
                            }
                            """))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IntegrationTransportException("Sony handshake returned HTTP " + response.statusCode());
            }

            return new SonyLanHandshakeResult(
                    request.endpoint(),
                    request.preSharedKey(),
                    "Validated Sony BRAVIA system endpoint with pre-shared key."
            );
        } catch (Exception exception) {
            if (IntegrationErrors.isTimeout(exception)) {
                throw new IntegrationTimeoutException("Sony handshake timed out.", exception);
            }
            throw new IntegrationTransportException("Sony handshake failed: " + exception.getMessage(), exception);
        }
    }
}
