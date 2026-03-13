package io.github.koyan9.tvremote.model;

import io.github.koyan9.tvremote.domain.ControlPath;

import java.util.List;

public record AdapterHealthStatus(
        String adapterKey,
        String brand,
        ControlPath path,
        String desiredMode,
        boolean enabled,
        boolean configComplete,
        boolean clientAvailable,
        boolean ready,
        List<String> missingConfig,
        List<String> availableClients,
        List<String> issues
) {
    public AdapterHealthStatus {
        missingConfig = missingConfig == null ? List.of() : List.copyOf(missingConfig);
        availableClients = availableClients == null ? List.of() : List.copyOf(availableClients);
        issues = issues == null ? List.of() : List.copyOf(issues);
    }
}
