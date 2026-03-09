package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.model.BrandOnboardingSessionSummary;
import io.github.koyan9.tvremote.persistence.CandidateDeviceEntity;

import java.util.List;

public interface BrandOnboardingProvider {

    String providerId();

    String brand();

    boolean supportsCandidate(CandidateDeviceEntity candidate);

    BrandOnboardingSessionSummary startOnboarding(String deviceId, String candidateId);

    List<BrandOnboardingSessionSummary> sessions(String deviceId);

    String latestNegotiatedCredential(String deviceId);
}
