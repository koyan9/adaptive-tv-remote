package io.github.koyan9.tvremote.integration;

import java.net.URI;

public record SamsungLanHandshakeResult(
        URI endpoint,
        String negotiatedToken,
        String detail
) {
}
