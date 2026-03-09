package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.DeviceType;
import io.github.koyan9.tvremote.domain.PairingStatus;
import io.github.koyan9.tvremote.model.DevicePairingRequest;
import io.github.koyan9.tvremote.model.DevicePairingSummary;
import io.github.koyan9.tvremote.model.DevicePairingUpdateRequest;
import io.github.koyan9.tvremote.persistence.DeviceEntity;
import io.github.koyan9.tvremote.persistence.DeviceRepository;
import io.github.koyan9.tvremote.persistence.PairingEntity;
import io.github.koyan9.tvremote.persistence.PairingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class PairingManagementService {

    private final PairingRepository pairingRepository;
    private final DeviceRepository deviceRepository;

    public PairingManagementService(PairingRepository pairingRepository, DeviceRepository deviceRepository) {
        this.pairingRepository = pairingRepository;
        this.deviceRepository = deviceRepository;
    }

    public List<DevicePairingSummary> pairingsForDevice(String deviceId) {
        verifyTargetDevice(deviceId);
        return pairingRepository.findAllByTargetDevice_IdOrderByUpdatedAtDesc(deviceId).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public DevicePairingSummary createPairing(DevicePairingRequest request) {
        DeviceEntity targetDevice = verifyTargetDevice(request.deviceId());
        if (!targetDevice.getAvailablePaths().contains(request.controlPath())) {
            throw new IllegalArgumentException("Device " + request.deviceId() + " does not support path " + request.controlPath());
        }

        DeviceEntity gatewayDevice = resolveGateway(targetDevice, request);
        ensureNotDuplicate(targetDevice, gatewayDevice, request.controlPath());
        revokeOtherActivePairings(targetDevice, request.controlPath(), null);

        PairingEntity entity = pairingRepository.save(new PairingEntity(
                UUID.randomUUID().toString(),
                targetDevice,
                gatewayDevice,
                request.controlPath(),
                PairingStatus.ACTIVE,
                request.externalReference(),
                request.notes() == null || request.notes().isBlank()
                        ? "Created by the first pairing management API version."
                        : request.notes()
        ));
        return toSummary(entity);
    }

    @Transactional
    public DevicePairingSummary updatePairing(String pairingId, DevicePairingUpdateRequest request) {
        PairingEntity entity = pairingRepository.findById(pairingId)
                .orElseThrow(() -> new NoSuchElementException("Pairing not found: " + pairingId));

        if (request.status() != null) {
            entity.setStatus(request.status());
            if (request.status() == PairingStatus.ACTIVE) {
                revokeOtherActivePairings(entity.getTargetDevice(), entity.getControlPath(), entity.getId());
            }
        }
        if (request.externalReference() != null) {
            entity.setExternalReference(request.externalReference());
        }
        if (request.notes() != null && !request.notes().isBlank()) {
            entity.setNotes(request.notes());
        }

        return toSummary(pairingRepository.save(entity));
    }

    @Transactional
    public void revokePairing(String pairingId) {
        PairingEntity entity = pairingRepository.findById(pairingId)
                .orElseThrow(() -> new NoSuchElementException("Pairing not found: " + pairingId));
        entity.setStatus(PairingStatus.REVOKED);
        entity.setNotes("Pairing revoked through the management API.");
        pairingRepository.save(entity);
    }

    public String resolveGatewayForRouting(String deviceId, ControlPath controlPath, String preferredGatewayId) {
        if (preferredGatewayId != null && !preferredGatewayId.isBlank()) {
            boolean matches = pairingRepository
                    .findAllByTargetDevice_IdAndControlPathAndStatusOrderByUpdatedAtDesc(deviceId, controlPath, PairingStatus.ACTIVE)
                    .stream()
                    .anyMatch(pairing -> pairing.getGatewayDevice() != null && preferredGatewayId.equals(pairing.getGatewayDevice().getId()));
            if (matches) {
                return preferredGatewayId;
            }
        }

        return pairingRepository
                .findAllByTargetDevice_IdAndControlPathAndStatusOrderByUpdatedAtDesc(deviceId, controlPath, PairingStatus.ACTIVE)
                .stream()
                .map(PairingEntity::getGatewayDevice)
                .filter(gateway -> gateway != null)
                .map(DeviceEntity::getId)
                .findFirst()
                .orElse(null);
    }

    public boolean hasPairingRecords(String deviceId, ControlPath controlPath) {
        return pairingRepository.countByTargetDevice_IdAndControlPath(deviceId, controlPath) > 0;
    }

    private DeviceEntity verifyTargetDevice(String deviceId) {
        DeviceEntity device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new NoSuchElementException("Device not found: " + deviceId));
        if (device.getDeviceType() == DeviceType.GATEWAY) {
            throw new IllegalArgumentException("Pairings can only be created for television devices.");
        }
        return device;
    }

    private DeviceEntity resolveGateway(DeviceEntity targetDevice, DevicePairingRequest request) {
        if (request.controlPath() == ControlPath.LAN_DIRECT) {
            if (request.gatewayDeviceId() != null && !request.gatewayDeviceId().isBlank()) {
                throw new IllegalArgumentException("gatewayDeviceId must be empty for LAN_DIRECT pairings.");
            }
            return null;
        }

        if (request.gatewayDeviceId() == null || request.gatewayDeviceId().isBlank()) {
            throw new IllegalArgumentException("gatewayDeviceId is required for gateway-based pairings.");
        }

        DeviceEntity gateway = deviceRepository.findById(request.gatewayDeviceId())
                .orElseThrow(() -> new NoSuchElementException("Gateway not found: " + request.gatewayDeviceId()));
        if (gateway.getDeviceType() != DeviceType.GATEWAY) {
            throw new IllegalArgumentException("Device " + request.gatewayDeviceId() + " is not a gateway.");
        }
        if (!gateway.getAvailablePaths().contains(request.controlPath())) {
            throw new IllegalArgumentException("Gateway " + request.gatewayDeviceId() + " does not support path " + request.controlPath());
        }
        if (!targetDevice.getAvailablePaths().contains(request.controlPath())) {
            throw new IllegalArgumentException("Device " + targetDevice.getId() + " does not support path " + request.controlPath());
        }
        return gateway;
    }

    private void ensureNotDuplicate(DeviceEntity targetDevice, DeviceEntity gatewayDevice, ControlPath controlPath) {
        boolean duplicate = pairingRepository
                .findAllByTargetDevice_IdAndControlPathAndStatusOrderByUpdatedAtDesc(targetDevice.getId(), controlPath, PairingStatus.ACTIVE)
                .stream()
                .anyMatch(pairing -> {
                    String existingGatewayId = pairing.getGatewayDevice() == null ? null : pairing.getGatewayDevice().getId();
                    String requestedGatewayId = gatewayDevice == null ? null : gatewayDevice.getId();
                    return java.util.Objects.equals(existingGatewayId, requestedGatewayId);
                });
        if (duplicate) {
            throw new IllegalArgumentException("An active pairing already exists for device " + targetDevice.getId() + " on path " + controlPath);
        }
    }

    private void revokeOtherActivePairings(DeviceEntity targetDevice, ControlPath controlPath, String excludePairingId) {
        pairingRepository.findAllByTargetDevice_IdAndControlPathAndStatusOrderByUpdatedAtDesc(
                        targetDevice.getId(),
                        controlPath,
                        PairingStatus.ACTIVE
                ).stream()
                .filter(pairing -> excludePairingId == null || !pairing.getId().equals(excludePairingId))
                .forEach(pairing -> {
                    pairing.setStatus(PairingStatus.REVOKED);
                    pairing.setNotes("Superseded by a newer active pairing.");
                    pairingRepository.save(pairing);
                });
    }

    private DevicePairingSummary toSummary(PairingEntity entity) {
        return new DevicePairingSummary(
                entity.getId(),
                entity.getTargetDevice().getId(),
                entity.getTargetDevice().getDisplayName(),
                entity.getControlPath(),
                entity.getGatewayDevice() == null ? null : entity.getGatewayDevice().getId(),
                entity.getGatewayDevice() == null ? null : entity.getGatewayDevice().getDisplayName(),
                entity.getStatus(),
                entity.getExternalReference(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
