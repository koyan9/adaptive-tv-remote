package io.github.koyan9.tvremote.model;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.PairingStatus;

import java.time.Instant;

public record DevicePairingSummary(
        String id,
        String deviceId,
        String deviceName,
        ControlPath controlPath,
        String gatewayDeviceId,
        String gatewayDeviceName,
        PairingStatus status,
        String externalReference,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
