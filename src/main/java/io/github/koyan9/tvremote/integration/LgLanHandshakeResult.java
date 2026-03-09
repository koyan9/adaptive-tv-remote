package io.github.koyan9.tvremote.integration;

import java.net.URI;

public record LgLanHandshakeResult(
        URI endpoint,
        String negotiatedClientKey,
        String detail
) {
}
