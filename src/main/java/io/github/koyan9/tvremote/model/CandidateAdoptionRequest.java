package io.github.koyan9.tvremote.model;

public record CandidateAdoptionRequest(
        String deviceId,
        String householdId,
        String roomId,
        String roomName,
        Boolean autoCreatePairings
) {
    public CandidateAdoptionRequest {
        autoCreatePairings = autoCreatePairings == null ? Boolean.TRUE : autoCreatePairings;
    }
}
