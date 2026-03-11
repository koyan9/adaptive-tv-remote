package io.github.koyan9.tvremote.integration;

import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletionException;

@Component
public class HttpLgLanHandshakeClient implements LgLanHandshakeClient {

    private final HttpClient httpClient;

    public HttpLgLanHandshakeClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    @Override
    public LgLanHandshakeResult startHandshake(LgLanHandshakeRequest request) {
        try {
            WebSocket webSocket = httpClient.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(3))
                    .buildAsync(request.endpoint(), new WebSocket.Listener() {
                    })
                    .join();

            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "handshake-opened").join();

            return new LgLanHandshakeResult(
                    request.endpoint(),
                    null,
                    "Opened an LG webOS onboarding handshake session. Client key capture is not implemented in the first LG onboarding version."
            );
        } catch (CompletionException exception) {
            Throwable cause = exception.getCause();
            if (IntegrationErrors.isTimeout(cause)) {
                throw new IntegrationTimeoutException("LG handshake timed out.", cause);
            }
            throw new IntegrationTransportException("LG handshake failed: " + cause.getMessage(), cause);
        } catch (Exception exception) {
            if (IntegrationErrors.isTimeout(exception)) {
                throw new IntegrationTimeoutException("LG handshake timed out.", exception);
            }
            throw new IntegrationTransportException("LG handshake failed: " + exception.getMessage(), exception);
        }
    }
}
