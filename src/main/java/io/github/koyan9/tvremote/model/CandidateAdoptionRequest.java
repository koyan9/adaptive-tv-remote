package io.github.koyan9.tvremote.model;

public record CandidateAdoptionRequest(
        String deviceId,
        String householdId,
        String roomId,
        String roomName,
        Boolean autoCreatePairings,
        Boolean autoStartSamsungHandshake,
        Boolean autoStartBrandOnboarding
) {
    public CandidateAdoptionRequest {
        autoCreatePairings = autoCreatePairings == null ? Boolean.TRUE : autoCreatePairings;
        autoStartBrandOnboarding = autoStartBrandOnboarding == null
                ? (autoStartSamsungHandshake == null ? Boolean.TRUE : autoStartSamsungHandshake)
                : autoStartBrandOnboarding;
        autoStartSamsungHandshake = autoStartSamsungHandshake == null ? autoStartBrandOnboarding : autoStartSamsungHandshake;
    }
}
