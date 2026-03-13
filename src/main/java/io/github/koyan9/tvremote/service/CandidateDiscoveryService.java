package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.domain.CandidateStatus;
import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.DeviceType;
import io.github.koyan9.tvremote.model.CandidateAdoptionRequest;
import io.github.koyan9.tvremote.model.DevicePairingRequest;
import io.github.koyan9.tvremote.model.DeviceRegistrationRequest;
import io.github.koyan9.tvremote.model.DiscoveryCandidateSummary;
import io.github.koyan9.tvremote.model.PairingSuggestion;
import io.github.koyan9.tvremote.model.RemoteDevice;
import io.github.koyan9.tvremote.persistence.CandidateDeviceEntity;
import io.github.koyan9.tvremote.persistence.CandidateDeviceRepository;
import io.github.koyan9.tvremote.persistence.DeviceEntity;
import io.github.koyan9.tvremote.persistence.DeviceRepository;
import io.github.koyan9.tvremote.persistence.HouseholdEntity;
import io.github.koyan9.tvremote.persistence.HouseholdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CandidateDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(CandidateDiscoveryService.class);

    private final CandidateDeviceRepository candidateDeviceRepository;
    private final DeviceRepository deviceRepository;
    private final HouseholdRepository householdRepository;
    private final DemoDiscoveryCandidateFactory demoDiscoveryCandidateFactory;
    private final RemoteManagementService remoteManagementService;
    private final PairingManagementService pairingManagementService;
    private final DeviceCatalogService deviceCatalogService;
    private final BrandOnboardingRegistry brandOnboardingRegistry;

    public CandidateDiscoveryService(
            CandidateDeviceRepository candidateDeviceRepository,
            DeviceRepository deviceRepository,
            HouseholdRepository householdRepository,
            DemoDiscoveryCandidateFactory demoDiscoveryCandidateFactory,
            RemoteManagementService remoteManagementService,
            PairingManagementService pairingManagementService,
            DeviceCatalogService deviceCatalogService,
            BrandOnboardingRegistry brandOnboardingRegistry
    ) {
        this.candidateDeviceRepository = candidateDeviceRepository;
        this.deviceRepository = deviceRepository;
        this.householdRepository = householdRepository;
        this.demoDiscoveryCandidateFactory = demoDiscoveryCandidateFactory;
        this.remoteManagementService = remoteManagementService;
        this.pairingManagementService = pairingManagementService;
        this.deviceCatalogService = deviceCatalogService;
        this.brandOnboardingRegistry = brandOnboardingRegistry;
    }

    @Transactional
    public List<DiscoveryCandidateSummary> scanCandidates() {
        reconcileCandidates();
        HouseholdEntity household = householdRepository.findFirstByOrderBySortOrderAsc()
                .orElseThrow(() -> new IllegalStateException("No household available for candidate discovery."));

        Map<String, CandidateDeviceEntity> existing = candidateDeviceRepository.findAllByOrderBySortOrderAsc().stream()
                .collect(Collectors.toMap(CandidateDeviceEntity::getId, entity -> entity, (left, right) -> left, LinkedHashMap::new));
        Set<String> seen = new HashSet<>();

        for (CandidateDeviceEntity candidate : demoDiscoveryCandidateFactory.candidates(household)) {
            seen.add(candidate.getId());
            CandidateDeviceEntity existingCandidate = existing.get(candidate.getId());
            if (existingCandidate == null) {
                candidateDeviceRepository.save(candidate);
                logger.info("Discovered new candidate {} via scan source {}", candidate.getId(), candidate.getDiscoverySource());
            } else {
                boolean changed = existingCandidate.refreshFromScan(candidate);
                existingCandidate.markSeen();
                candidateDeviceRepository.save(existingCandidate);
                if (changed) {
                    logger.info("Refreshed candidate {} from scan source {}", existingCandidate.getId(), candidate.getDiscoverySource());
                }
            }
        }

        for (CandidateDeviceEntity candidate : existing.values()) {
            if (seen.contains(candidate.getId())) {
                continue;
            }
            if (candidate.getStatus() == CandidateStatus.DISCOVERED && candidate.isOnline()) {
                candidate.setOnline(false);
                candidateDeviceRepository.save(candidate);
                logger.info("Candidate {} missing from scan; marking offline.", candidate.getId());
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
        return pairingSuggestions(candidateId, null);
    }

    public List<PairingSuggestion> pairingSuggestions(String candidateId, String networkName) {
        CandidateDeviceEntity candidate = getCandidate(candidateId);
        String normalizedNetworkName = normalizeNetworkName(networkName);
        return candidate.getPreferredPaths().stream()
                .filter(candidate.getAvailablePaths()::contains)
                .flatMap(controlPath -> suggestionsForPath(candidate, controlPath, normalizedNetworkName).stream())
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
        reconcileCandidates();
        CandidateDeviceEntity candidate = getCandidate(candidateId);
        if (candidate.getStatus() == CandidateStatus.ADOPTED) {
            String adoptedDeviceId = candidate.getAdoptedDeviceId();
            if (adoptedDeviceId != null && deviceRepository.existsById(adoptedDeviceId)) {
                candidate.markSeen();
                candidateDeviceRepository.save(candidate);
                return deviceCatalogService.getDevice(adoptedDeviceId);
            }
        }
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

        DeviceEntity existingDevice = deviceRepository.findById(deviceId).orElse(null);
        RemoteDevice device;
        if (existingDevice != null) {
            if (existingDevice.getDeviceType() == DeviceType.GATEWAY) {
                throw new IllegalArgumentException("Device " + deviceId + " is a gateway and cannot be adopted.");
            }
            if (!existingDevice.getBrand().equalsIgnoreCase(candidate.getBrand())
                    || !existingDevice.getModel().equalsIgnoreCase(candidate.getModel())) {
                throw new IllegalArgumentException("Device " + deviceId + " already exists with a different brand/model.");
            }
            device = deviceCatalogService.getDevice(deviceId);
        } else {
            device = remoteManagementService.registerDevice(new DeviceRegistrationRequest(
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
        }

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
        reconcileCandidates();
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

    private void reconcileCandidates() {
        List<CandidateDeviceEntity> candidates = candidateDeviceRepository.findAllByOrderBySortOrderAsc();
        Map<String, List<CandidateDeviceEntity>> byAdoptedDevice = new LinkedHashMap<>();

        for (CandidateDeviceEntity candidate : candidates) {
            String adoptedDeviceId = candidate.getAdoptedDeviceId();
            if (adoptedDeviceId == null || adoptedDeviceId.isBlank()) {
                if (candidate.getStatus() == CandidateStatus.ADOPTED) {
                    logger.info("Candidate {} marked adopted without device; resetting to discovered.", candidate.getId());
                    candidate.setStatus(CandidateStatus.DISCOVERED);
                    candidateDeviceRepository.save(candidate);
                }
                continue;
            }
            adoptedDeviceId = adoptedDeviceId.trim();
            if (!deviceRepository.existsById(adoptedDeviceId)) {
                logger.info("Candidate {} references missing device {}; clearing adoption.", candidate.getId(), adoptedDeviceId);
                candidate.setStatus(CandidateStatus.DISCOVERED);
                candidate.setAdoptedDeviceId(null);
                candidateDeviceRepository.save(candidate);
                continue;
            }
            if (candidate.getStatus() != CandidateStatus.ADOPTED) {
                logger.info("Candidate {} now linked to device {}; marking adopted.", candidate.getId(), adoptedDeviceId);
                candidate.setStatus(CandidateStatus.ADOPTED);
                candidateDeviceRepository.save(candidate);
            }
            byAdoptedDevice.computeIfAbsent(adoptedDeviceId, ignored -> new java.util.ArrayList<>()).add(candidate);
        }

        for (List<CandidateDeviceEntity> group : byAdoptedDevice.values()) {
            if (group.size() <= 1) {
                continue;
            }
            group.sort(java.util.Comparator.comparing(CandidateDeviceEntity::getUpdatedAt).reversed());
            CandidateDeviceEntity winner = group.get(0);
            if (winner.getStatus() != CandidateStatus.ADOPTED) {
                winner.setStatus(CandidateStatus.ADOPTED);
                candidateDeviceRepository.save(winner);
            }
            logger.info("Resolved duplicate adoption for device {}. Keeping candidate {} and resetting {} others.",
                    winner.getAdoptedDeviceId(), winner.getId(), group.size() - 1);
            for (int i = 1; i < group.size(); i++) {
                CandidateDeviceEntity candidate = group.get(i);
                candidate.setStatus(CandidateStatus.DISCOVERED);
                candidate.setAdoptedDeviceId(null);
                candidateDeviceRepository.save(candidate);
            }
        }
    }

    private List<PairingSuggestion> suggestionsForPath(CandidateDeviceEntity candidate, ControlPath controlPath, String normalizedNetworkName) {
        if (controlPath == ControlPath.LAN_DIRECT) {
            if (candidate.isSameWifiRequired() && normalizedNetworkName != null) {
                String expectedNetworkName = normalizeNetworkName(candidate.getHousehold().getNetworkName());
                if (expectedNetworkName != null && !expectedNetworkName.equalsIgnoreCase(normalizedNetworkName)) {
                    return List.of();
                }
            }
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
            if (pairingManagementService.hasActivePairingRecords(deviceId, suggestion.controlPath())) {
                continue;
            }
            pairingManagementService.createPairing(new DevicePairingRequest(
                    deviceId,
                    suggestion.controlPath(),
                    suggestion.gatewayDeviceId(),
                    "candidate-adoption:" + suggestion.candidateId(),
                    suggestion.rationale()
            ));
        }
    }

    private String normalizeNetworkName(String networkName) {
        if (networkName == null) {
            return null;
        }
        String trimmed = networkName.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
                entity.getLastSeenAt(),
                entity.getUpdatedAt(),
                entity.getCreatedAt(),
                entity.isSameWifiRequired()
        );
    }
}
