package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.model.DeviceCapability;
import io.github.koyan9.tvremote.model.ModelProfile;
import io.github.koyan9.tvremote.model.RemoteDevice;
import io.github.koyan9.tvremote.persistence.DeviceEntity;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;

@Component
public class DeviceMapper {

    public RemoteDevice toDomain(DeviceEntity entity) {
        return new RemoteDevice(
                entity.getId(),
                entity.getDisplayName(),
                entity.getDeviceType(),
                entity.getBrand(),
                entity.getModel(),
                entity.getRoom().getName(),
                entity.isOnline(),
                new LinkedHashSet<>(entity.getAvailablePaths()),
                List.copyOf(entity.getLinkedGatewayIds()),
                new DeviceCapability(
                        entity.isSameWifiRequired(),
                        entity.isRequiresPairing(),
                        entity.isSupportsWakeOnLan(),
                        new LinkedHashSet<>(entity.getSupportedCommands())
                ),
                new ModelProfile(
                        entity.getBrand(),
                        entity.getModel(),
                        entity.getProfileMarketingName(),
                        List.copyOf(entity.getPreferredPaths()),
                        entity.getProfileNotes()
                )
        );
    }
}
