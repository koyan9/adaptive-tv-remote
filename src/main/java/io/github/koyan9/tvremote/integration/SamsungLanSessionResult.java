package io.github.koyan9.tvremote.integration;

import java.net.URI;

public record SamsungLanSessionResult(
        URI endpoint,
        String detail
) {
}
