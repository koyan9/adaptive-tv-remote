package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.domain.DeviceType;
import io.github.koyan9.tvremote.model.DeviceCapability;
import io.github.koyan9.tvremote.model.ModelProfile;
import io.github.koyan9.tvremote.model.RemoteDevice;
import io.github.koyan9.tvremote.persistence.DeviceEntity;
import io.github.koyan9.tvremote.persistence.DeviceRepository;
import io.github.koyan9.tvremote.persistence.HouseholdRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DeviceCatalogService {

    private final DeviceRepository deviceRepository;
    private final HouseholdRepository householdRepository;
    private final DeviceMapper deviceMapper;

    public DeviceCatalogService(
            DeviceRepository deviceRepository,
            HouseholdRepository householdRepository,
            DeviceMapper deviceMapper
    ) {
        this.deviceRepository = deviceRepository;
        this.householdRepository = householdRepository;
        this.deviceMapper = deviceMapper;
    }

    public List<RemoteDevice> televisionDevices() {
        return deviceRepository.findAllByOrderBySortOrderAsc().stream()
                .map(deviceMapper::toDomain)
                .filter(RemoteDevice::isTelevision)
                .toList();
    }

    public List<RemoteDevice> allDevices() {
        return deviceRepository.findAllByOrderBySortOrderAsc().stream()
                .map(deviceMapper::toDomain)
                .toList();
    }

    public RemoteDevice getDevice(String deviceId) {
        return deviceRepository.findById(deviceId)
                .map(deviceMapper::toDomain)
                .orElseThrow(() -> new NoSuchElementException("Device not found: " + deviceId));
    }

    public List<RemoteDevice> gateways() {
        return deviceRepository.findAllByOrderBySortOrderAsc().stream()
                .map(deviceMapper::toDomain)
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
}


