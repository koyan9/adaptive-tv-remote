package io.github.koyan9.tvremote.model;

import io.github.koyan9.tvremote.domain.ControlPath;

import java.util.List;

public record ControlDecision(
        ControlPath path,
        String gatewayDeviceId,
        String adapterLabel,
        List<ControlPath> attemptedPaths,
        String reason
) {
    public ControlDecision {
        attemptedPaths = List.copyOf(attemptedPaths);
    }
}


