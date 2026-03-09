package io.github.koyan9.tvremote.integration;

import java.net.URI;

public record GatewayHdmiCecCommandRequest(
        URI endpoint,
        String authToken,
        String hubId,
        String payload,
        String actionKey
) {
}
