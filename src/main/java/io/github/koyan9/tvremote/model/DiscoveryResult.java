package io.github.koyan9.tvremote.model;

import java.time.Instant;
import java.util.List;

public record DiscoveryResult(
        String networkName,
        Instant scannedAt,
        List<RemoteDevice> devices
) {
    public DiscoveryResult {
        devices = List.copyOf(devices);
    }
}


