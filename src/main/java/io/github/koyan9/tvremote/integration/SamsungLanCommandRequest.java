package io.github.koyan9.tvremote.integration;

import java.net.URI;

public record SamsungLanCommandRequest(
        URI endpoint,
        String payload,
        String remoteKey
) {
}
