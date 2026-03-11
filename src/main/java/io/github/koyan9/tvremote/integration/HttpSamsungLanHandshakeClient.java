package io.github.koyan9.tvremote.integration;

import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

@Component
public class HttpSamsungLanHandshakeClient implements SamsungLanHandshakeClient {

    private final HttpClient httpClient;
    private static final long WS_TIMEOUT_SECONDS = 3;

    public HttpSamsungLanHandshakeClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    @Override
    public SamsungLanHandshakeResult startHandshake(SamsungLanHandshakeRequest request) {
        try {
            WebSocket webSocket = httpClient.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(WS_TIMEOUT_SECONDS))
                    .buildAsync(request.endpoint(), new WebSocket.Listener() {
                    })
                    .orTimeout(WS_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .join();

            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "handshake-opened")
                    .orTimeout(WS_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .join();

            return new SamsungLanHandshakeResult(
                    request.endpoint(),
                    null,
                    "Opened a Samsung LAN pairing handshake session. Token capture is not implemented in the first real handshake version."
            );
        } catch (CompletionException exception) {
            Throwable cause = exception.getCause();
            if (IntegrationErrors.isTimeout(cause)) {
                throw new IntegrationTimeoutException("Samsung handshake timed out.", cause);
            }
            throw new IntegrationTransportException("Samsung handshake failed: " + cause.getMessage(), cause);
        } catch (Exception exception) {
            if (IntegrationErrors.isTimeout(exception)) {
                throw new IntegrationTimeoutException("Samsung handshake timed out.", exception);
            }
            throw new IntegrationTransportException("Samsung handshake failed: " + exception.getMessage(), exception);
        }
    }
}
