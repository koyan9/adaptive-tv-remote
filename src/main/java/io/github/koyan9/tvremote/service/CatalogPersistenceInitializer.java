package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.model.RemoteDevice;
import io.github.koyan9.tvremote.persistence.DeviceEntity;
import io.github.koyan9.tvremote.persistence.DeviceRepository;
import io.github.koyan9.tvremote.persistence.HouseholdEntity;
import io.github.koyan9.tvremote.persistence.HouseholdRepository;
import io.github.koyan9.tvremote.persistence.RoomEntity;
import io.github.koyan9.tvremote.persistence.RoomRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class CatalogPersistenceInitializer implements ApplicationRunner {

    private final HouseholdRepository householdRepository;
    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;
    private final DemoCatalogFactory demoCatalogFactory;

    public CatalogPersistenceInitializer(
            HouseholdRepository householdRepository,
            RoomRepository roomRepository,
            DeviceRepository deviceRepository,
            DemoCatalogFactory demoCatalogFactory
    ) {
        this.householdRepository = householdRepository;
        this.roomRepository = roomRepository;
        this.deviceRepository = deviceRepository;
        this.demoCatalogFactory = demoCatalogFactory;
    }

    @Override
    @Transactional
    public void run(org.springframework.boot.ApplicationArguments args) {
        if (deviceRepository.count() > 0) {
            return;
        }

        DemoCatalogFactory.DemoHousehold demoHousehold = demoCatalogFactory.household();
        HouseholdEntity household = householdRepository.save(new HouseholdEntity(
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

    private String roomId(String roomName) {
        return roomName.toLowerCase().replace(' ', '-');
    }
}
