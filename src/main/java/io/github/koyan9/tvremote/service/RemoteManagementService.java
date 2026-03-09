package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.model.DeviceRegistrationRequest;
import io.github.koyan9.tvremote.model.HouseholdSummary;
import io.github.koyan9.tvremote.model.RemoteDevice;
import io.github.koyan9.tvremote.model.RoomSummary;
import io.github.koyan9.tvremote.persistence.DeviceEntity;
import io.github.koyan9.tvremote.persistence.DeviceRepository;
import io.github.koyan9.tvremote.persistence.HouseholdEntity;
import io.github.koyan9.tvremote.persistence.HouseholdRepository;
import io.github.koyan9.tvremote.persistence.RoomEntity;
import io.github.koyan9.tvremote.persistence.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

@Service
public class RemoteManagementService {

    private final HouseholdRepository householdRepository;
    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    public RemoteManagementService(
            HouseholdRepository householdRepository,
            RoomRepository roomRepository,
            DeviceRepository deviceRepository,
            DeviceMapper deviceMapper
    ) {
        this.householdRepository = householdRepository;
        this.roomRepository = roomRepository;
        this.deviceRepository = deviceRepository;
        this.deviceMapper = deviceMapper;
    }

    public List<HouseholdSummary> households() {
        return householdRepository.findAllByOrderBySortOrderAsc().stream()
                .map(household -> new HouseholdSummary(
                        household.getId(),
                        household.getName(),
                        household.getNetworkName(),
                        roomRepository.countByHousehold_Id(household.getId()),
                        deviceRepository.countByRoom_Household_Id(household.getId())
                ))
                .toList();
    }

    public List<RoomSummary> rooms(String householdId) {
        List<RoomEntity> rooms = householdId == null || householdId.isBlank()
                ? roomRepository.findAllByOrderBySortOrderAsc()
                : roomRepository.findAllByHousehold_IdOrderBySortOrderAsc(householdId);

        return rooms.stream()
                .map(room -> new RoomSummary(
                        room.getId(),
                        room.getHousehold().getId(),
                        room.getHousehold().getName(),
                        room.getName(),
                        room.getSortOrder(),
                        deviceRepository.countByRoom_Id(room.getId())
                ))
                .toList();
    }

    @Transactional
    public RemoteDevice registerDevice(DeviceRegistrationRequest request) {
        if (deviceRepository.existsById(request.deviceId())) {
            throw new IllegalArgumentException("Device already exists: " + request.deviceId());
        }

        HouseholdEntity household = householdRepository.findById(request.householdId())
                .orElseThrow(() -> new NoSuchElementException("Household not found: " + request.householdId()));
        RoomEntity room = resolveRoom(household, request);

        DeviceEntity entity = deviceRepository.save(new DeviceEntity(
                request.deviceId(),
                deviceRepository.findAllByOrderBySortOrderAsc().size(),
                request.displayName(),
                request.deviceType(),
                request.brand(),
                request.model(),
                Boolean.TRUE.equals(request.online()),
                room,
                request.availablePaths(),
                request.linkedGatewayIds(),
                Boolean.TRUE.equals(request.sameWifiRequired()),
                Boolean.TRUE.equals(request.requiresPairing()),
                Boolean.TRUE.equals(request.supportsWakeOnLan()),
                request.supportedCommands(),
                request.profileMarketingName(),
                request.preferredPaths(),
                request.profileNotes()
        ));

        return deviceMapper.toDomain(entity);
    }

    private RoomEntity resolveRoom(HouseholdEntity household, DeviceRegistrationRequest request) {
        if (request.roomId() != null && !request.roomId().isBlank()) {
            RoomEntity room = roomRepository.findById(request.roomId())
                    .orElseThrow(() -> new NoSuchElementException("Room not found: " + request.roomId()));
            if (!room.getHousehold().getId().equals(household.getId())) {
                throw new IllegalArgumentException("Room " + request.roomId() + " does not belong to household " + household.getId());
            }
            return room;
        }

        if (request.roomName() == null || request.roomName().isBlank()) {
            throw new IllegalArgumentException("roomName is required when roomId is not provided.");
        }

        return roomRepository.findByHousehold_IdAndNameIgnoreCase(household.getId(), request.roomName())
                .orElseGet(() -> roomRepository.save(new RoomEntity(
                        roomId(request.roomName()),
                        request.roomName(),
                        roomRepository.findAllByHousehold_IdOrderBySortOrderAsc(household.getId()).size(),
                        household
                )));
    }

    private String roomId(String roomName) {
        return roomName.toLowerCase(Locale.ROOT)
                .replace(' ', '-')
                .replaceAll("[^a-z0-9-]", "-")
                .replaceAll("-+", "-");
    }
}
