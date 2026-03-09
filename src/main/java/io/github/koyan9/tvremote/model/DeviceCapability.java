package io.github.koyan9.tvremote.model;

import io.github.koyan9.tvremote.domain.RemoteCommand;

import java.util.Set;

public record DeviceCapability(
        boolean sameWifiRequired,
        boolean requiresPairing,
        boolean supportsWakeOnLan,
        Set<RemoteCommand> supportedCommands
) {
    public DeviceCapability {
        supportedCommands = Set.copyOf(supportedCommands);
    }

    public boolean supports(RemoteCommand command) {
        return supportedCommands.contains(command);
    }
}


