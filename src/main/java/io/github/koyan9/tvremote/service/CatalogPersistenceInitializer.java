package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.model.RemoteDevice;
import io.github.koyan9.tvremote.persistence.PairingEntity;
import io.github.koyan9.tvremote.persistence.PairingRepository;
import io.github.koyan9.tvremote.persistence.DeviceEntity;
import io.github.koyan9.tvremote.persistence.DeviceRepository;
import io.github.koyan9.tvremote.persistence.HouseholdEntity;
import io.github.koyan9.tvremote.persistence.HouseholdRepository;
import io.github.koyan9.tvremote.persistence.RoomEntity;
import io.github.koyan9.tvremote.persistence.RoomRepository;
import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.PairingStatus;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class CatalogPersistenceInitializer implements ApplicationRunner {

    private final HouseholdRepository householdRepository;
    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;
    private final PairingRepository pairingRepository;
    private final DemoCatalogFactory demoCatalogFactory;

    public CatalogPersistenceInitializer(
            HouseholdRepository householdRepository,
            RoomRepository roomRepository,
            DeviceRepository deviceRepository,
            PairingRepository pairingRepository,
            DemoCatalogFactory demoCatalogFactory
    ) {
        this.householdRepository = householdRepository;
        this.roomRepository = roomRepository;
        this.deviceRepository = deviceRepository;
        this.pairingRepository = pairingRepository;
        this.demoCatalogFactory = demoCatalogFactory;
    }

    @Override
    @Transactional
    public void run(org.springframework.boot.ApplicationArguments args) {
        HouseholdEntity household;
        if (deviceRepository.count() == 0) {
            DemoCatalogFactory.DemoHousehold demoHousehold = demoCatalogFactory.household();
            household = householdRepository.save(new HouseholdEntity(
                    demoHousehold.id(),
                    demoHousehold.name(),
                    demoHousehold.networkName(),
                    demoHousehold.sortOrder()
            ));

            List<RemoteDevice> devices = demoCatalogFactory.devices();
            Map<String, RoomEntity> rooms = new LinkedHashMap<>();
            for (RemoteDevice device : devices) {
                if (!rooms.containsKey(device.room())) {
                    RoomEntity room = roomRepository.save(new RoomEntity(
                            roomId(device.room()),
                            device.room(),
                            rooms.size(),
                            household
                    ));
                    rooms.put(device.room(), room);
                }
            }

            int deviceOrder = 0;
            for (RemoteDevice device : devices) {
                RoomEntity room = rooms.get(device.room());
                deviceRepository.save(new DeviceEntity(
                        device.id(),
                        deviceOrder++,
                        device.displayName(),
                        device.deviceType(),
                        device.brand(),
                        device.model(),
                        device.online(),
                        room,
                        device.availablePaths(),
                        device.linkedGatewayIds(),
                        device.capability().sameWifiRequired(),
                        device.capability().requiresPairing(),
                        device.capability().supportsWakeOnLan(),
                        device.capability().supportedCommands(),
                        device.profile().marketingName(),
                        device.profile().preferredPaths(),
                        device.profile().notes()
                ));
            }
        }

        if (pairingRepository.count() == 0) {
            seedPairings();
        }
    }

    private String roomId(String roomName) {
        return roomName.toLowerCase().replace(' ', '-');
    }

    private void seedPairings() {
        savePairing("tv-living-room", null, ControlPath.LAN_DIRECT, "samsung-lan-session", "Seeded direct Samsung LAN pairing.");
        savePairing("tv-bedroom", "gateway-home-hub", ControlPath.IR_GATEWAY, "sony-ir-seed", "Seeded Sony fallback infrared pairing.");
        savePairing("tv-office", null, ControlPath.LAN_DIRECT, "sony-lan-session", "Seeded direct Sony LAN pairing.");
        savePairing("tv-studio", null, ControlPath.LAN_DIRECT, "lg-lan-session", "Seeded direct LG LAN pairing.");
        savePairing("tv-family-room", "gateway-home-hub", ControlPath.IR_GATEWAY, "tcl-ir-seed", "Seeded TCL infrared pairing.");
        savePairing("tv-guest-room", "gateway-home-hub", ControlPath.IR_GATEWAY, "hisense-ir-seed", "Seeded legacy infrared pairing.");
        savePairing("tv-cinema-room", "gateway-home-hub", ControlPath.HDMI_CEC_GATEWAY, "cec-seed", "Seeded HDMI-CEC gateway pairing.");
    }

    private void savePairing(String deviceId, String gatewayId, ControlPath path, String externalReference, String notes) {
        DeviceEntity target = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalStateException("Missing seeded device: " + deviceId));
        DeviceEntity gateway = gatewayId == null ? null : deviceRepository.findById(gatewayId)
                .orElseThrow(() -> new IllegalStateException("Missing seeded gateway: " + gatewayId));

        pairingRepository.save(new PairingEntity(
                UUID.randomUUID().toString(),
                target,
                gateway,
                path,
                PairingStatus.ACTIVE,
                externalReference,
                notes
        ));
    }
}
