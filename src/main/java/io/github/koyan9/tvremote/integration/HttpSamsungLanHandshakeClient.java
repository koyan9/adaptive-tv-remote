package io.github.koyan9.tvremote.integration;

import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletionException;

@Component
public class HttpSamsungLanHandshakeClient implements SamsungLanHandshakeClient {

    private final HttpClient httpClient;

    public HttpSamsungLanHandshakeClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    @Override
    public SamsungLanHandshakeResult startHandshake(SamsungLanHandshakeRequest request) {
        try {
            WebSocket webSocket = httpClient.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(3))
                    .buildAsync(request.endpoint(), new WebSocket.Listener() {
                    })
                    .join();

            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "handshake-opened").join();

            return new SamsungLanHandshakeResult(
                    request.endpoint(),
                    null,
                    "Opened a Samsung LAN pairing handshake session. Token capture is not implemented in the first real handshake version."
            );
        } catch (CompletionException exception) {
            throw new IllegalStateException("Samsung handshake failed: " + exception.getCause().getMessage(), exception.getCause());
        } catch (Exception exception) {
            throw new IllegalStateException("Samsung handshake failed: " + exception.getMessage(), exception);
        }
    }
}
