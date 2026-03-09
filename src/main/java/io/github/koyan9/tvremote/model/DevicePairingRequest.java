package io.github.koyan9.tvremote.model;

import io.github.koyan9.tvremote.domain.ControlPath;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DevicePairingRequest(
        @NotBlank String deviceId,
        @NotNull ControlPath controlPath,
        String gatewayDeviceId,
        String externalReference,
        String notes
) {
}
