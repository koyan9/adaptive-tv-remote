package io.github.koyan9.tvremote.model;

import java.time.Instant;

public record DeviceOnboardingStatusSummary(
        String deviceId,
        String deviceName,
        String brand,
        boolean onboardingSupported,
        int sessionCount,
        String latestProviderId,
        String latestStatus,
        String latestDetail,
        String latestFailureReason,
        boolean negotiatedCredentialPresent,
        String credentialPreview,
        Instant latestUpdatedAt
) {
}
