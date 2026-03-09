package io.github.koyan9.tvremote.integration;

import java.net.URI;

public record GatewayHdmiCecSessionResult(
        URI endpoint,
        int statusCode,
        String detail
) {
}
