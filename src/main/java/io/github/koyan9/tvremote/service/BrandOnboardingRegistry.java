package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.model.BrandOnboardingSessionSummary;
import io.github.koyan9.tvremote.persistence.CandidateDeviceEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

@Service
public class BrandOnboardingRegistry {

    private final List<BrandOnboardingProvider> providers;

    public BrandOnboardingRegistry(List<BrandOnboardingProvider> providers) {
        this.providers = List.copyOf(providers);
    }

    public BrandOnboardingSessionSummary startForCandidate(String deviceId, CandidateDeviceEntity candidate) {
        return providerForCandidate(candidate).startOnboarding(deviceId, candidate.getId());
    }

    public List<BrandOnboardingSessionSummary> sessions(String deviceId, String brand) {
        if (brand == null || brand.isBlank()) {
            return providers.stream()
                    .flatMap(provider -> provider.sessions(deviceId).stream())
                    .toList();
        }
        return providerForBrand(brand).sessions(deviceId);
    }

    public String latestNegotiatedCredential(String deviceId, String brand) {
        return providerForBrand(brand).latestNegotiatedCredential(deviceId);
    }

    private BrandOnboardingProvider providerForCandidate(CandidateDeviceEntity candidate) {
        return providers.stream()
                .filter(provider -> provider.supportsCandidate(candidate))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No onboarding provider found for candidate " + candidate.getId()));
    }

    private BrandOnboardingProvider providerForBrand(String brand) {
        return providers.stream()
                .filter(provider -> provider.brand().equalsIgnoreCase(brand))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No onboarding provider found for brand " + brand.toLowerCase(Locale.ROOT)));
    }
}
