package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.DeviceType;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.DeviceCapability;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class DeviceCatalogService {

    private final Map<String, RemoteDevice> devices;

    public DeviceCatalogService(ModelProfileService modelProfileService) {
        Set<RemoteCommand> fullRemote = EnumSet.allOf(RemoteCommand.class);
        Set<RemoteCommand> basicRemote = EnumSet.of(
                RemoteCommand.POWER_TOGGLE,
                RemoteCommand.POWER_ON,
                RemoteCommand.POWER_OFF,
                RemoteCommand.VOLUME_UP,
                RemoteCommand.VOLUME_DOWN,
                RemoteCommand.MUTE,
                RemoteCommand.HOME,
                RemoteCommand.BACK,
                RemoteCommand.INPUT_SOURCE,
                RemoteCommand.DPAD_UP,
                RemoteCommand.DPAD_DOWN,
                RemoteCommand.DPAD_LEFT,
                RemoteCommand.DPAD_RIGHT,
                RemoteCommand.OK
        );

        Map<String, RemoteDevice> seeded = new LinkedHashMap<>();
        seeded.put("tv-living-room", new RemoteDevice(
                "tv-living-room",
                "Living Room Samsung",
                DeviceType.SMART_TV,
                "Samsung",
                "QN90",
                "Living Room",
                true,
                EnumSet.of(ControlPath.LAN_DIRECT, ControlPath.IR_GATEWAY, ControlPath.HDMI_CEC_GATEWAY),
                List.of("gateway-home-hub"),
                new DeviceCapability(true, true, true, fullRemote),
                modelProfileService.samsungLivingRoomProfile()
        ));
        seeded.put("tv-bedroom", new RemoteDevice(
                "tv-bedroom",
                "Bedroom Sony",
                DeviceType.SMART_TV,
                "Sony",
                "XR-55A80",
                "Bedroom",
                false,
                EnumSet.of(ControlPath.LAN_DIRECT, ControlPath.IR_GATEWAY),
                List.of("gateway-home-hub"),
                new DeviceCapability(true, true, false, fullRemote),
                modelProfileService.sonyBedroomProfile()
        ));
        seeded.put("tv-office", new RemoteDevice(
                "tv-office",
                "Office Sony",
                DeviceType.SMART_TV,
                "Sony",
                "XR-65X90L",
                "Office",
                true,
                EnumSet.of(ControlPath.LAN_DIRECT, ControlPath.IR_GATEWAY, ControlPath.HDMI_CEC_GATEWAY),
                List.of("gateway-home-hub"),
                new DeviceCapability(true, true, true, fullRemote),
                modelProfileService.sonyOfficeProfile()
        ));
        seeded.put("tv-studio", new RemoteDevice(
                "tv-studio",
                "Studio LG",
                DeviceType.SMART_TV,
                "LG",
                "OLED55C3",
                "Studio",
                true,
                EnumSet.of(ControlPath.LAN_DIRECT, ControlPath.IR_GATEWAY, ControlPath.HDMI_CEC_GATEWAY),
                List.of("gateway-home-hub"),
                new DeviceCapability(true, true, true, fullRemote),
                modelProfileService.lgStudioProfile()
        ));
        seeded.put("tv-guest-room", new RemoteDevice(
                "tv-guest-room",
                "Guest Room Legacy TV",
                DeviceType.LEGACY_TV,
                "Hisense",
                "H32A4",
                "Guest Room",
                false,
                EnumSet.of(ControlPath.IR_GATEWAY),
                List.of("gateway-home-hub"),
                new DeviceCapability(false, false, false, basicRemote),
                modelProfileService.legacyGuestRoomProfile()
        ));
        seeded.put("tv-cinema-room", new RemoteDevice(
                "tv-cinema-room",
                "Cinema Room Display",
                DeviceType.LEGACY_TV,
                "BenQ",
                "W4000i",
                "Cinema Room",
                false,
                EnumSet.of(ControlPath.HDMI_CEC_GATEWAY, ControlPath.IR_GATEWAY),
                List.of("gateway-home-hub"),
                new DeviceCapability(false, false, false, basicRemote),
                modelProfileService.cinemaRoomProfile()
        ));
        seeded.put("gateway-home-hub", new RemoteDevice(
                "gateway-home-hub",
                "Home Hub One",
                DeviceType.GATEWAY,
                "Koyan",
                "Home Hub One",
                "Hallway",
                true,
                EnumSet.of(ControlPath.IR_GATEWAY, ControlPath.HDMI_CEC_GATEWAY),
                List.of(),
                new DeviceCapability(false, false, false, basicRemote),
                modelProfileService.gatewayProfile()
        ));
        this.devices = seeded;
    }

    public List<RemoteDevice> televisionDevices() {
        return devices.values().stream()
                .filter(RemoteDevice::isTelevision)
                .toList();
    }

    public List<RemoteDevice> allDevices() {
        return List.copyOf(devices.values());
    }

    public RemoteDevice getDevice(String deviceId) {
        RemoteDevice device = devices.get(deviceId);
        if (device == null) {
            throw new NoSuchElementException("Device not found: " + deviceId);
        }
        return device;
    }

    public Collection<RemoteDevice> gateways() {
        return devices.values().stream()
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
}


