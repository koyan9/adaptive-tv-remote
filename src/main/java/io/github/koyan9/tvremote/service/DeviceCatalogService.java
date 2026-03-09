package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.domain.DeviceType;
import io.github.koyan9.tvremote.model.DeviceCapability;
import io.github.koyan9.tvremote.model.ModelProfile;
import io.github.koyan9.tvremote.model.RemoteDevice;
import io.github.koyan9.tvremote.persistence.DeviceEntity;
import io.github.koyan9.tvremote.persistence.DeviceRepository;
import io.github.koyan9.tvremote.persistence.HouseholdRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DeviceCatalogService {

    private final DeviceRepository deviceRepository;
    private final HouseholdRepository householdRepository;

    public DeviceCatalogService(DeviceRepository deviceRepository, HouseholdRepository householdRepository) {
        this.deviceRepository = deviceRepository;
        this.householdRepository = householdRepository;
    }

    public List<RemoteDevice> televisionDevices() {
        return deviceRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toDomain)
                .filter(RemoteDevice::isTelevision)
                .toList();
    }

    public List<RemoteDevice> allDevices() {
        return deviceRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    public RemoteDevice getDevice(String deviceId) {
        return deviceRepository.findById(deviceId)
                .map(this::toDomain)
                .orElseThrow(() -> new NoSuchElementException("Device not found: " + deviceId));
    }

    public Collection<RemoteDevice> gateways() {
        return deviceRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toDomain)
                .filter(device -> device.deviceType() == DeviceType.GATEWAY)
                .toList();
    }

    public RemoteDevice getGateway(String gatewayId) {
        RemoteDevice gateway = getDevice(gatewayId);
        if (gateway.deviceType() != DeviceType.GATEWAY) {
            throw new NoSuchElementException("Gateway not found: " + gatewayId);
        }
        return gateway;
    }

    public String primaryHouseholdNetworkName() {
        return householdRepository.findFirstByOrderBySortOrderAsc()
                .map(household -> household.getNetworkName())
                .orElse("Unknown Network");
    }

    private RemoteDevice toDomain(DeviceEntity entity) {
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


