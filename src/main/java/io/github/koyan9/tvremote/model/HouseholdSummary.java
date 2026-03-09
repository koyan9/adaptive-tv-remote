package io.github.koyan9.tvremote.model;

public record HouseholdSummary(
        String id,
        String name,
        String networkName,
        int roomCount,
        int deviceCount
) {
}
