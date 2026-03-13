package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.model.ControlDecision;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ControlRoutingService {

    private final DeviceCatalogService deviceCatalogService;
    private final PairingManagementService pairingManagementService;

    public ControlRoutingService(
            DeviceCatalogService deviceCatalogService,
            PairingManagementService pairingManagementService
    ) {
        this.deviceCatalogService = deviceCatalogService;
        this.pairingManagementService = pairingManagementService;
    }

    public ControlDecision chooseRoute(RemoteDevice device, String preferredGatewayId, String networkName) {
        List<ControlPath> attempted = new ArrayList<>();
        String normalizedNetworkName = normalizeNetworkName(networkName);
        String expectedNetworkName = null;
        boolean lanBlockedByWifi = false;
        for (ControlPath candidate : device.profile().preferredPaths()) {
            attempted.add(candidate);
            if (!device.availablePaths().contains(candidate)) {
                continue;
            }

            if (candidate == ControlPath.LAN_DIRECT) {
                if (device.capability().sameWifiRequired() && normalizedNetworkName != null) {
                    if (expectedNetworkName == null) {
                        expectedNetworkName = normalizeNetworkName(deviceCatalogService.householdNetworkNameForDevice(device.id()));
                    }
                    if (expectedNetworkName != null && !expectedNetworkName.equalsIgnoreCase(normalizedNetworkName)) {
                        lanBlockedByWifi = true;
                        continue;
                    }
                }
                if (device.capability().requiresPairing()
                        && !pairingManagementService.hasPairingRecords(device.id(), ControlPath.LAN_DIRECT)) {
                    continue;
                }
                boolean canAttemptLan = device.online() || device.capability().supportsWakeOnLan();
                if (canAttemptLan) {
                    String reason = device.online()
                            ? "The TV is online on the home Wi-Fi, so the app uses direct local control."
                            : "The TV is offline but supports Wake-on-LAN, so the app attempts direct control.";
                    return new ControlDecision(
                            ControlPath.LAN_DIRECT,
                            null,
                            "LAN Direct Adapter",
                            attempted,
                            reason
                    );
                }
            }

            if (candidate == ControlPath.IR_GATEWAY || candidate == ControlPath.HDMI_CEC_GATEWAY) {
                if (device.capability().requiresPairing()
                        && !pairingManagementService.hasPairingRecords(device.id(), candidate)) {
                    continue;
                }
                RemoteDevice gateway = resolveGateway(device, candidate, preferredGatewayId);
                if (gateway != null && gateway.online() && gateway.availablePaths().contains(candidate)) {
                    return new ControlDecision(
                            candidate,
                            gateway.id(),
                            candidate == ControlPath.IR_GATEWAY ? "Infrared Gateway Adapter" : "HDMI-CEC Gateway Adapter",
                            attempted,
                            "Direct control is unavailable, so the request falls back to the paired home gateway."
                    );
                }
            }
        }

        if (lanBlockedByWifi) {
            String detail = expectedNetworkName == null
                    ? "LAN direct control requires the same Wi-Fi network."
                    : "LAN direct control requires the same Wi-Fi network. Expected \"" + expectedNetworkName + "\" but received \"" + normalizedNetworkName + "\".";
            throw new ControlRoutingException(detail, ControlRoutingFailureReason.WIFI_MISMATCH, attempted);
        }

        throw new ControlRoutingException(
                "No viable control path is available for device " + device.displayName(),
                ControlRoutingFailureReason.NO_VIABLE_PATH,
                attempted
        );
    }

    private RemoteDevice resolveGateway(RemoteDevice device, ControlPath controlPath, String preferredGatewayId) {
        String pairedGatewayId = pairingManagementService.resolveGatewayForRouting(device.id(), controlPath, preferredGatewayId);
        if (pairedGatewayId != null) {
            try {
                return deviceCatalogService.getGateway(pairedGatewayId);
            } catch (Exception ignored) {
            }
        }

        if (pairingManagementService.hasPairingRecords(device.id(), controlPath)) {
            return null;
        }

        if (preferredGatewayId != null && !preferredGatewayId.isBlank()) {
            try {
                return deviceCatalogService.getGateway(preferredGatewayId);
            } catch (Exception ignored) {
            }
        }

        for (String gatewayId : device.linkedGatewayIds()) {
            try {
                return deviceCatalogService.getGateway(gatewayId);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String normalizeNetworkName(String networkName) {
        if (networkName == null) {
            return null;
        }
        String trimmed = networkName.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}


