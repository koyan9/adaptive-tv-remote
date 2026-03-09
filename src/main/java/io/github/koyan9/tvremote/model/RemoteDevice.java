package io.github.koyan9.tvremote.model;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.DeviceType;

import java.util.List;
import java.util.Set;

public record RemoteDevice(
        String id,
        String displayName,
        DeviceType deviceType,
        String brand,
        String model,
        String room,
        boolean online,
        Set<ControlPath> availablePaths,
        List<String> linkedGatewayIds,
        DeviceCapability capability,
        ModelProfile profile
) {
    public RemoteDevice {
        availablePaths = Set.copyOf(availablePaths);
        linkedGatewayIds = List.copyOf(linkedGatewayIds);
    }

    public boolean isTelevision() {
        return deviceType == DeviceType.SMART_TV || deviceType == DeviceType.LEGACY_TV;
    }
}


