package io.github.koyan9.tvremote.integration;

import java.net.URI;

public record SonyLanHandshakeResult(
        URI endpoint,
        String negotiatedPreSharedKey,
        String detail
) {
}
