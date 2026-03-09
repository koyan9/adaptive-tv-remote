package io.github.koyan9.tvremote.model;

import io.github.koyan9.tvremote.domain.SamsungHandshakeStatus;

import java.time.Instant;

public record SamsungHandshakeSummary(
        String id,
        String deviceId,
        String deviceName,
        String candidateId,
        String endpoint,
        String clientName,
        String negotiatedToken,
        SamsungHandshakeStatus status,
        String detail,
        Instant createdAt,
        Instant updatedAt
) {
}
