package io.github.koyan9.tvremote.model;

import io.github.koyan9.tvremote.domain.ControlPath;

public record PairingSuggestion(
        String candidateId,
        ControlPath controlPath,
        String gatewayDeviceId,
        String gatewayDeviceName,
        String rationale,
        boolean autoSelectable
) {
}
