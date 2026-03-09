package io.github.koyan9.tvremote.model;

import io.github.koyan9.tvremote.domain.ControlPath;

import java.util.List;

public record ModelProfile(
        String brand,
        String model,
        String marketingName,
        List<ControlPath> preferredPaths,
        String notes
) {
    public ModelProfile {
        preferredPaths = List.copyOf(preferredPaths);
    }
}


