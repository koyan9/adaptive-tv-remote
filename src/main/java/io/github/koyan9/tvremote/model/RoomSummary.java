package io.github.koyan9.tvremote.model;

public record RoomSummary(
        String id,
        String householdId,
        String householdName,
        String name,
        int sortOrder,
        int deviceCount
) {
}
