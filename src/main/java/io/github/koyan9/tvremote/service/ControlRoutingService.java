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

    public ControlDecision chooseRoute(RemoteDevice device, String preferredGatewayId) {
        List<ControlPath> attempted = new ArrayList<>();
        for (ControlPath candidate : device.profile().preferredPaths()) {
            attempted.add(candidate);
            if (!device.availablePaths().contains(candidate)) {
                continue;
            }

            if (candidate == ControlPath.LAN_DIRECT && device.online()) {
                return new ControlDecision(
                        ControlPath.LAN_DIRECT,
                        null,
                        "LAN Direct Adapter",
                        attempted,
                        "The TV is online on the home Wi-Fi, so the app uses direct local control."
                );
            }

            if (candidate == ControlPath.IR_GATEWAY || candidate == ControlPath.HDMI_CEC_GATEWAY) {
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

        throw new ControlRoutingException("No viable control path is available for device " + device.displayName());
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
}


