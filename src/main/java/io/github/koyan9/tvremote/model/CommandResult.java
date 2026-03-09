package io.github.koyan9.tvremote.model;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.RemoteCommand;

import java.time.Instant;

public record CommandResult(
        String correlationId,
        String deviceId,
        String deviceName,
        RemoteCommand command,
        ControlPath route,
        String gatewayDeviceId,
        String adapterLabel,
        String brandAdapterKey,
        String protocolFamily,
        String protocolClientKey,
        String integrationMode,
        String integrationEndpoint,
        String status,
        String message,
        Instant executedAt
) {
}


