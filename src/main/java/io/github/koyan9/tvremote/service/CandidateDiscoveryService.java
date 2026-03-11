package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.domain.CandidateStatus;
import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.model.CandidateAdoptionRequest;
import io.github.koyan9.tvremote.model.DevicePairingRequest;
import io.github.koyan9.tvremote.model.DeviceRegistrationRequest;
import io.github.koyan9.tvremote.model.DiscoveryCandidateSummary;
import io.github.koyan9.tvremote.model.PairingSuggestion;
import io.github.koyan9.tvremote.model.RemoteDevice;
import io.github.koyan9.tvremote.persistence.CandidateDeviceEntity;
import io.github.koyan9.tvremote.persistence.CandidateDeviceRepository;
import io.github.koyan9.tvremote.persistence.HouseholdEntity;
import io.github.koyan9.tvremote.persistence.HouseholdRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CandidateDiscoveryService {

    private final CandidateDeviceRepository candidateDeviceRepository;
    private final HouseholdRepository householdRepository;
    private final DemoDiscoveryCandidateFactory demoDiscoveryCandidateFactory;
    private final RemoteManagementService remoteManagementService;
    private final PairingManagementService pairingManagementService;
    private final DeviceCatalogService deviceCatalogService;
    private final BrandOnboardingRegistry brandOnboardingRegistry;

    public CandidateDiscoveryService(
            CandidateDeviceRepository candidateDeviceRepository,
            HouseholdRepository householdRepository,
            DemoDiscoveryCandidateFactory demoDiscoveryCandidateFactory,
            RemoteManagementService remoteManagementService,
            PairingManagementService pairingManagementService,
            DeviceCatalogService deviceCatalogService,
            BrandOnboardingRegistry brandOnboardingRegistry
    ) {
        this.candidateDeviceRepository = candidateDeviceRepository;
        this.householdRepository = householdRepository;
        this.demoDiscoveryCandidateFactory = demoDiscoveryCandidateFactory;
        this.remoteManagementService = remoteManagementService;
        this.pairingManagementService = pairingManagementService;
        this.deviceCatalogService = deviceCatalogService;
        this.brandOnboardingRegistry = brandOnboardingRegistry;
    }

    @Transactional
    public List<DiscoveryCandidateSummary> scanCandidates() {
        HouseholdEntity household = householdRepository.findFirstByOrderBySortOrderAsc()
                .orElseThrow(() -> new IllegalStateException("No household available for candidate discovery."));

        Map<String, CandidateDeviceEntity> existing = candidateDeviceRepository.findAllByOrderBySortOrderAsc().stream()
                .collect(Collectors.toMap(CandidateDeviceEntity::getId, entity -> entity, (left, right) -> left, LinkedHashMap::new));

        for (CandidateDeviceEntity candidate : demoDiscoveryCandidateFactory.candidates(household)) {
            CandidateDeviceEntity existingCandidate = existing.get(candidate.getId());
            if (existingCandidate == null) {
                candidateDeviceRepository.save(candidate);
            } else {
                existingCandidate.markSeen();
                candidateDeviceRepository.save(existingCandidate);
            }
        }
        return candidates(CandidateStatus.DISCOVERED);
    }

    public List<DiscoveryCandidateSummary> candidates(CandidateStatus status) {
        List<CandidateDeviceEntity> entities = status == null
                ? candidateDeviceRepository.findAllByOrderBySortOrderAsc()
                : candidateDeviceRepository.findAllByStatusOrderBySortOrderAsc(status);
        return entities.stream().map(this::toSummary).toList();
    }

    public List<PairingSuggestion> pairingSuggestions(String candidateId) {
        CandidateDeviceEntity candidate = getCandidate(candidateId);
        return candidate.getPreferredPaths().stream()
                .filter(candidate.getAvailablePaths()::contains)
                .flatMap(controlPath -> suggestionsForPath(candidate, controlPath).stream())
                .toList();
    }

    @Transactional
    public DiscoveryCandidateSummary dismissCandidate(String candidateId) {
        CandidateDeviceEntity candidate = getCandidate(candidateId);
        if (candidate.getStatus() == CandidateStatus.ADOPTED) {
            throw new IllegalArgumentException("Adopted candidates cannot be dismissed.");
        }
        candidate.setStatus(CandidateStatus.DISMISSED);
        candidate.markSeen();
        candidateDeviceRepository.save(candidate);
        return toSummary(candidate);
    }

    @Transactional
    public DiscoveryCandidateSummary reopenCandidate(String candidateId) {
        CandidateDeviceEntity candidate = getCandidate(candidateId);
        if (candidate.getStatus() != CandidateStatus.DISMISSED) {
            throw new IllegalArgumentException("Only dismissed candidates can be reopened.");
        }
        candidate.setStatus(CandidateStatus.DISCOVERED);
        candidate.markSeen();
        candidateDeviceRepository.save(candidate);
        return toSummary(candidate);
    }

    @Transactional
    public RemoteDevice adoptCandidate(String candidateId, CandidateAdoptionRequest request) {
        CandidateDeviceEntity candidate = getCandidate(candidateId);
        if (candidate.getStatus() != CandidateStatus.DISCOVERED) {
            throw new IllegalArgumentException("Candidate " + candidateId + " is not available for adoption.");
        }

        String householdId = request.householdId() == null || request.householdId().isBlank()
                ? candidate.getHousehold().getId()
                : request.householdId();
        String deviceId = request.deviceId() == null || request.deviceId().isBlank()
                ? defaultDeviceId(candidate)
                : request.deviceId();

        List<PairingSuggestion> suggestions = pairingSuggestions(candidateId);
        List<String> linkedGatewayIds = suggestions.stream()
                .map(PairingSuggestion::gatewayDeviceId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        RemoteDevice device = remoteManagementService.registerDevice(new DeviceRegistrationRequest(
                deviceId,
                candidate.getDisplayName(),
                candidate.getDeviceType(),
                candidate.getBrand(),
                candidate.getModel(),
                householdId,
                request.roomId(),
                request.roomName() == null || request.roomName().isBlank() ? candidate.getRoomName() : request.roomName(),
                candidate.isOnline(),
                candidate.getAvailablePaths(),
                linkedGatewayIds,
                candidate.isSameWifiRequired(),
                candidate.isRequiresPairing(),
                candidate.isSupportsWakeOnLan(),
                candidate.getSupportedCommands(),
                candidate.getProfileMarketingName(),
                candidate.getPreferredPaths(),
                candidate.getProfileNotes()
        ));

        if (Boolean.TRUE.equals(request.autoCreatePairings())) {
            createSuggestedPairings(device.id(), suggestions);
        }

        if (Boolean.TRUE.equals(request.autoStartBrandOnboarding()) && supportsBrandOnboarding(candidate)) {
            brandOnboardingRegistry.startForCandidate(device.id(), candidate);
        }

        candidate.setStatus(CandidateStatus.ADOPTED);
        candidate.setAdoptedDeviceId(device.id());
        candidate.markSeen();
        candidateDeviceRepository.save(candidate);
        return device;
    }

    @Transactional
    public io.github.koyan9.tvremote.model.BrandOnboardingSessionSummary retryOnboardingForDevice(String deviceId, String brand) {
        CandidateDeviceEntity candidate = candidateDeviceRepository.findAllByAdoptedDeviceIdOrderByUpdatedAtDesc(deviceId).stream()
                .filter(found -> brand == null || brand.isBlank() || found.getBrand().equalsIgnoreCase(brand))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No adopted candidate found for device " + deviceId));

        if (!supportsBrandOnboarding(candidate)) {
            throw new IllegalArgumentException("Device " + deviceId + " does not support brand onboarding retry.");
        }

        return brandOnboardingRegistry.startForCandidate(deviceId, candidate);
    }

    private CandidateDeviceEntity getCandidate(String candidateId) {
        return candidateDeviceRepository.findById(candidateId)
                .orElseThrow(() -> new NoSuchElementException("Candidate not found: " + candidateId));
    }

    private List<PairingSuggestion> suggestionsForPath(CandidateDeviceEntity candidate, ControlPath controlPath) {
        if (controlPath == ControlPath.LAN_DIRECT) {
            if (!candidate.isOnline() && !candidate.isSupportsWakeOnLan()) {
                return List.of();
            }
            String availability = candidate.isOnline()
                    ? "Candidate is online and supports direct LAN control on the home network."
                    : "Candidate supports Wake-on-LAN for direct LAN control.";
            String wifiNote = candidate.isSameWifiRequired()
                    ? " Same Wi-Fi required."
                    : "";
            return List.of(new PairingSuggestion(
                    candidate.getId(),
                    controlPath,
                    null,
                    null,
                    availability + wifiNote,
                    true
            ));
        }

        return deviceCatalogService.gateways().stream()
                .filter(gateway -> gateway.availablePaths().contains(controlPath))
                .filter(RemoteDevice::online)
                .map(gateway -> new PairingSuggestion(
                        candidate.getId(),
                        controlPath,
                        gateway.id(),
                        gateway.displayName(),
                        "Gateway " + gateway.displayName() + " can bridge " + controlPath + " for this candidate.",
                        true
                ))
                .toList();
    }

    private void createSuggestedPairings(String deviceId, List<PairingSuggestion> suggestions) {
        Map<ControlPath, PairingSuggestion> selected = new LinkedHashMap<>();
        for (PairingSuggestion suggestion : suggestions) {
            selected.putIfAbsent(suggestion.controlPath(), suggestion);
        }

        for (PairingSuggestion suggestion : selected.values()) {
            pairingManagementService.createPairing(new DevicePairingRequest(
                    deviceId,
                    suggestion.controlPath(),
                    suggestion.gatewayDeviceId(),
                    "candidate-adoption:" + suggestion.candidateId(),
                    suggestion.rationale()
            ));
        }
    }

    private String defaultDeviceId(CandidateDeviceEntity candidate) {
        return candidate.getId()
                .replaceFirst("^candidate-", "tv-")
                .toLowerCase(Locale.ROOT);
    }

    private boolean supportsBrandOnboarding(CandidateDeviceEntity candidate) {
        return candidate.getAvailablePaths().contains(ControlPath.LAN_DIRECT)
                && (
                "Samsung".equalsIgnoreCase(candidate.getBrand())
                        || "LG".equalsIgnoreCase(candidate.getBrand())
                        || "Sony".equalsIgnoreCase(candidate.getBrand())
        );
    }

    private DiscoveryCandidateSummary toSummary(CandidateDeviceEntity entity) {
        return new DiscoveryCandidateSummary(
                entity.getId(),
                entity.getHousehold().getId(),
                entity.getHousehold().getName(),
                entity.getDisplayName(),
                entity.getDeviceType(),
                entity.getBrand(),
                entity.getModel(),
                entity.getRoomName(),
                entity.isOnline(),
                entity.getAvailablePaths(),
                entity.getPreferredPaths(),
                entity.getStatus(),
                entity.getAdoptedDeviceId(),
                entity.getDiscoverySource(),
                entity.getLastSeenAt()
        );
    }
}
