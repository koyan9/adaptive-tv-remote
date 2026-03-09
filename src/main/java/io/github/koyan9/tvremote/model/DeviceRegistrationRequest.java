package io.github.koyan9.tvremote.model;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.DeviceType;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;

public record DeviceRegistrationRequest(
        @NotBlank String deviceId,
        @NotBlank String displayName,
        @NotNull DeviceType deviceType,
        @NotBlank String brand,
        @NotBlank String model,
        @NotBlank String householdId,
        String roomId,
        String roomName,
        Boolean online,
        @NotEmpty Set<ControlPath> availablePaths,
        List<String> linkedGatewayIds,
        Boolean sameWifiRequired,
        Boolean requiresPairing,
        Boolean supportsWakeOnLan,
        @NotEmpty Set<RemoteCommand> supportedCommands,
        @NotBlank String profileMarketingName,
        @NotEmpty List<ControlPath> preferredPaths,
        @NotBlank String profileNotes
) {
    public DeviceRegistrationRequest {
        availablePaths = availablePaths == null ? Set.of() : Set.copyOf(availablePaths);
        linkedGatewayIds = linkedGatewayIds == null ? List.of() : List.copyOf(linkedGatewayIds);
        supportedCommands = supportedCommands == null ? Set.of() : Set.copyOf(supportedCommands);
        preferredPaths = preferredPaths == null ? List.of() : List.copyOf(preferredPaths);
        online = online == null ? Boolean.FALSE : online;
        sameWifiRequired = sameWifiRequired == null ? Boolean.FALSE : sameWifiRequired;
        requiresPairing = requiresPairing == null ? Boolean.FALSE : requiresPairing;
        supportsWakeOnLan = supportsWakeOnLan == null ? Boolean.FALSE : supportsWakeOnLan;
    }
}
