package io.github.koyan9.tvremote.integration;

import io.github.koyan9.tvremote.domain.RemoteCommand;

public record SonyIrccCommandRequest(
        String endpoint,
        String preSharedKey,
        RemoteCommand command,
        String irccCode,
        String payload
) {
}
