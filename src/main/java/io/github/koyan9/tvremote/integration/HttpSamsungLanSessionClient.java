package io.github.koyan9.tvremote.integration;

import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletionException;

@Component
public class HttpSamsungLanSessionClient implements SamsungLanSessionClient {

    private final HttpClient httpClient;

    public HttpSamsungLanSessionClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    @Override
    public SamsungLanSessionResult sendCommand(SamsungLanCommandRequest request) {
        try {
            WebSocket webSocket = httpClient.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(3))
                    .buildAsync(request.endpoint(), new WebSocket.Listener() {
                    })
                    .join();

            webSocket.sendText(request.payload(), true).join();
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "command-sent").join();

            return new SamsungLanSessionResult(
                    request.endpoint(),
                    "Opened a Samsung LAN WebSocket session and sent remote key " + request.remoteKey() + "."
            );
        } catch (CompletionException exception) {
            Throwable cause = exception.getCause();
            if (IntegrationErrors.isTimeout(cause)) {
                throw new IntegrationTimeoutException("Samsung LAN request timed out.", cause);
            }
            throw new IntegrationTransportException("Samsung LAN request failed: " + cause.getMessage(), cause);
        } catch (Exception exception) {
            if (IntegrationErrors.isTimeout(exception)) {
                throw new IntegrationTimeoutException("Samsung LAN request timed out.", exception);
            }
            throw new IntegrationTransportException("Samsung LAN request failed: " + exception.getMessage(), exception);
        }
    }
}
