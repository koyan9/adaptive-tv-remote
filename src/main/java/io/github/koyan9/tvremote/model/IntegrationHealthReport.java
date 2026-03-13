package io.github.koyan9.tvremote.model;

import java.util.List;

public record IntegrationHealthReport(
        String defaultMode,
        boolean strictMode,
        List<AdapterHealthStatus> adapters
) {
    public IntegrationHealthReport {
        adapters = adapters == null ? List.of() : List.copyOf(adapters);
    }
}
