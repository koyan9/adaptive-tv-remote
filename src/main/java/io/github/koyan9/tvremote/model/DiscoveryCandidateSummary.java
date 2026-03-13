package io.github.koyan9.tvremote.model;

import io.github.koyan9.tvremote.domain.CandidateStatus;
import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.DeviceType;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public record DiscoveryCandidateSummary(
        String id,
        String householdId,
        String householdName,
        String displayName,
        DeviceType deviceType,
        String brand,
        String model,
        String roomName,
        boolean online,
        Set<ControlPath> availablePaths,
        List<ControlPath> preferredPaths,
        CandidateStatus status,
        String adoptedDeviceId,
        String discoverySource,
        Instant lastSeenAt,
        Instant updatedAt,
        Instant createdAt,
        boolean sameWifiRequired
) {
}
