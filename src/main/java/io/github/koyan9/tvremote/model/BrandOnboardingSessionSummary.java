package io.github.koyan9.tvremote.model;

import java.time.Instant;

public record BrandOnboardingSessionSummary(
        String sessionId,
        String providerId,
        String brand,
        String deviceId,
        String deviceName,
        String candidateId,
        String endpoint,
        String clientIdentity,
        String negotiatedCredential,
        String status,
        String detail,
        Instant createdAt,
        Instant updatedAt
) {
}
