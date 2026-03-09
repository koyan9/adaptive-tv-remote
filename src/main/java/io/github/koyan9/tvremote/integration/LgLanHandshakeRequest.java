package io.github.koyan9.tvremote.integration;

import java.net.URI;

public record LgLanHandshakeRequest(
        URI endpoint,
        String clientIdentity,
        String candidateId,
        String deviceId
) {
}
