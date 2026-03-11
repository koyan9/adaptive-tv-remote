package io.github.koyan9.tvremote.integration;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class HttpSonyLanSessionClient implements SonyLanSessionClient {

    private final HttpClient httpClient;

    public HttpSonyLanSessionClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    @Override
    public SonyLanSessionResult sendCommand(SonyIrccCommandRequest request) {
        try {
            URI endpoint = URI.create(request.endpoint());
            HttpRequest httpRequest = HttpRequest.newBuilder(endpoint)
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "text/xml; charset=UTF-8")
                    .header("SOAPACTION", "\"urn:schemas-sony-com:service:IRCC:1#X_SendIRCC\"")
                    .header("X-Auth-PSK", request.preSharedKey())
                    .POST(HttpRequest.BodyPublishers.ofString(request.payload()))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IntegrationTransportException("Sony IRCC returned HTTP " + response.statusCode());
            }

            return new SonyLanSessionResult(
                    endpoint,
                    response.statusCode(),
                    "Sent Sony IRCC command " + request.command() + " using code " + request.irccCode() + "."
            );
        } catch (Exception exception) {
            if (IntegrationErrors.isTimeout(exception)) {
                throw new IntegrationTimeoutException("Sony IRCC request timed out.", exception);
            }
            throw new IntegrationTransportException("Sony IRCC request failed: " + exception.getMessage(), exception);
        }
    }
}
