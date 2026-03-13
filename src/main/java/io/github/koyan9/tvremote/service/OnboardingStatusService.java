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
        String failureReason = classifyFailure(latest);

        return new DeviceOnboardingStatusSummary(
                device.id(),
                device.displayName(),
                device.brand(),
                supported,
                sessions.size(),
                latest == null ? null : latest.providerId(),
                latest == null ? null : latest.status(),
                latest == null ? null : latest.detail(),
                failureReason,
                credential != null && !credential.isBlank(),
                preview(credential),
                latest == null ? null : latest.updatedAt()
        );
    }

    private String classifyFailure(BrandOnboardingSessionSummary latest) {
        if (latest == null || latest.status() == null || !"FAILED".equalsIgnoreCase(latest.status())) {
            return null;
        }
        String detail = latest.detail() == null ? "" : latest.detail().toLowerCase();
        if (detail.contains("timeout")) {
            return io.github.koyan9.tvremote.model.OnboardingFailureReason.TIMEOUT.name();
        }
        if (detail.contains("unauthorized")
                || detail.contains("forbidden")
                || detail.contains("401")
                || detail.contains("403")
                || detail.contains("auth")) {
            return io.github.koyan9.tvremote.model.OnboardingFailureReason.AUTH_FAILURE.name();
        }
        if (detail.contains("unsupported")
                || detail.contains("not supported")
                || detail.contains("disabled")
                || detail.contains("missing")
                || detail.contains("no protocol client")) {
            return io.github.koyan9.tvremote.model.OnboardingFailureReason.PROTOCOL_UNSUPPORTED.name();
        }
        return io.github.koyan9.tvremote.model.OnboardingFailureReason.UNKNOWN.name();
    }

    private String preview(String credential) {
        if (credential == null || credential.isBlank()) {
            return null;
        }
        if (credential.length() <= 6) {
            return credential;
        }
        return credential.substring(0, 4) + "..." + credential.substring(credential.length() - 2);
    }
}
