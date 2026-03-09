package io.github.koyan9.tvremote.model;

import io.github.koyan9.tvremote.domain.PairingStatus;

public record DevicePairingUpdateRequest(
        PairingStatus status,
        String externalReference,
        String notes
) {
}
