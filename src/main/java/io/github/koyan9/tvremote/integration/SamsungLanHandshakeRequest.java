package io.github.koyan9.tvremote.integration;

import java.net.URI;

public record SamsungLanHandshakeRequest(
        URI endpoint,
        String clientName,
        String candidateId,
        String deviceId
) {
}
