package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.model.BrandOnboardingSessionSummary;
import io.github.koyan9.tvremote.model.DeviceOnboardingStatusSummary;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class OnboardingStatusService {

    private final DeviceCatalogService deviceCatalogService;
    private final BrandOnboardingRegistry brandOnboardingRegistry;

    public OnboardingStatusService(
            DeviceCatalogService deviceCatalogService,
            BrandOnboardingRegistry brandOnboardingRegistry
    ) {
        this.deviceCatalogService = deviceCatalogService;
        this.brandOnboardingRegistry = brandOnboardingRegistry;
    }

    public DeviceOnboardingStatusSummary statusForDevice(String deviceId) {
        RemoteDevice device = deviceCatalogService.getDevice(deviceId);
        boolean supported = brandOnboardingRegistry.supportsBrand(device.brand());
        List<BrandOnboardingSessionSummary> sessions = supported
                ? brandOnboardingRegistry.sessions(deviceId, device.brand())
                : List.of();

        BrandOnboardingSessionSummary latest = sessions.isEmpty() ? null : sessions.get(0);
        String credential = supported ? brandOnboardingRegistry.latestNegotiatedCredential(deviceId, device.brand()) : null;

        return new DeviceOnboardingStatusSummary(
                device.id(),
                device.displayName(),
                device.brand(),
                supported,
                sessions.size(),
                latest == null ? null : latest.providerId(),
                latest == null ? null : latest.status(),
                latest == null ? null : latest.detail(),
                credential != null && !credential.isBlank(),
                preview(credential),
                latest == null ? null : latest.updatedAt()
        );
    }

    private String preview(String credential) {
        if (credential == null || credential.isBlank()) {
            return null;
        }
        if (credential.length() <= 6) {
            return credential;
        }
        return credential.substring(0, 4) + "…" + credential.substring(credential.length() - 2);
    }
}
