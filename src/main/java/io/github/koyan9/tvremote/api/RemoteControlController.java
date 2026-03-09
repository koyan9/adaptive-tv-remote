package io.github.koyan9.tvremote.api;

import io.github.koyan9.tvremote.brand.AdapterDescriptor;
import io.github.koyan9.tvremote.brand.BrandAdapterRegistry;
import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import io.github.koyan9.tvremote.config.RemoteProjectProperties;
import io.github.koyan9.tvremote.integration.ProtocolClientRegistry;
import io.github.koyan9.tvremote.model.BrandOnboardingSessionSummary;
import io.github.koyan9.tvremote.model.CandidateAdoptionRequest;
import io.github.koyan9.tvremote.model.CommandRequest;
import io.github.koyan9.tvremote.model.CommandResult;
import io.github.koyan9.tvremote.model.DevicePairingRequest;
import io.github.koyan9.tvremote.model.DevicePairingSummary;
import io.github.koyan9.tvremote.model.DevicePairingUpdateRequest;
import io.github.koyan9.tvremote.model.DeviceOnboardingStatusSummary;
import io.github.koyan9.tvremote.model.DeviceRegistrationRequest;
import io.github.koyan9.tvremote.model.DiscoveryCandidateSummary;
import io.github.koyan9.tvremote.model.DiscoveryResult;
import io.github.koyan9.tvremote.model.HouseholdSummary;
import io.github.koyan9.tvremote.model.PairingSuggestion;
import io.github.koyan9.tvremote.model.RemoteDevice;
import io.github.koyan9.tvremote.model.RoomSummary;
import io.github.koyan9.tvremote.model.SamsungHandshakeSummary;
import io.github.koyan9.tvremote.service.BrandOnboardingRegistry;
import io.github.koyan9.tvremote.service.ControlExecutionService;
import io.github.koyan9.tvremote.service.CandidateDiscoveryService;
import io.github.koyan9.tvremote.service.DeviceCatalogService;
import io.github.koyan9.tvremote.service.DiscoveryService;
import io.github.koyan9.tvremote.service.OnboardingStatusService;
import io.github.koyan9.tvremote.service.PairingManagementService;
import io.github.koyan9.tvremote.service.RemoteManagementService;
import io.github.koyan9.tvremote.service.SamsungOnboardingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/remote")
public class RemoteControlController {

    private final DeviceCatalogService deviceCatalogService;
    private final DiscoveryService discoveryService;
    private final ControlExecutionService controlExecutionService;
    private final CandidateDiscoveryService candidateDiscoveryService;
    private final BrandAdapterRegistry brandAdapterRegistry;
    private final RemoteProjectProperties remoteProjectProperties;
    private final ProtocolClientRegistry protocolClientRegistry;
    private final RemoteIntegrationProperties remoteIntegrationProperties;
    private final RemoteManagementService remoteManagementService;
    private final PairingManagementService pairingManagementService;
    private final SamsungOnboardingService samsungOnboardingService;
    private final BrandOnboardingRegistry brandOnboardingRegistry;
    private final OnboardingStatusService onboardingStatusService;

    public RemoteControlController(
            DeviceCatalogService deviceCatalogService,
            DiscoveryService discoveryService,
            ControlExecutionService controlExecutionService,
            CandidateDiscoveryService candidateDiscoveryService,
            BrandAdapterRegistry brandAdapterRegistry,
            RemoteProjectProperties remoteProjectProperties,
            ProtocolClientRegistry protocolClientRegistry,
            RemoteIntegrationProperties remoteIntegrationProperties,
            RemoteManagementService remoteManagementService,
            PairingManagementService pairingManagementService,
            SamsungOnboardingService samsungOnboardingService,
            BrandOnboardingRegistry brandOnboardingRegistry,
            OnboardingStatusService onboardingStatusService
    ) {
        this.deviceCatalogService = deviceCatalogService;
        this.discoveryService = discoveryService;
        this.controlExecutionService = controlExecutionService;
        this.candidateDiscoveryService = candidateDiscoveryService;
        this.brandAdapterRegistry = brandAdapterRegistry;
        this.remoteProjectProperties = remoteProjectProperties;
        this.protocolClientRegistry = protocolClientRegistry;
        this.remoteIntegrationProperties = remoteIntegrationProperties;
        this.remoteManagementService = remoteManagementService;
        this.pairingManagementService = pairingManagementService;
        this.samsungOnboardingService = samsungOnboardingService;
        this.brandOnboardingRegistry = brandOnboardingRegistry;
        this.onboardingStatusService = onboardingStatusService;
    }

    @GetMapping("/households")
    public List<HouseholdSummary> households() {
        return remoteManagementService.households();
    }

    @GetMapping("/rooms")
    public List<RoomSummary> rooms(@RequestParam(required = false) String householdId) {
        return remoteManagementService.rooms(householdId);
    }

    @GetMapping("/devices/{deviceId}/pairings")
    public List<DevicePairingSummary> pairings(@PathVariable String deviceId) {
        return pairingManagementService.pairingsForDevice(deviceId);
    }

    @GetMapping("/devices/{deviceId}/onboarding/samsung-handshakes")
    public List<SamsungHandshakeSummary> samsungHandshakes(@PathVariable String deviceId) {
        return samsungOnboardingService.handshakes(deviceId);
    }

    @GetMapping("/devices/{deviceId}/onboarding/sessions")
    public List<BrandOnboardingSessionSummary> onboardingSessions(
            @PathVariable String deviceId,
            @RequestParam(required = false) String brand
    ) {
        return brandOnboardingRegistry.sessions(deviceId, brand);
    }

    @GetMapping("/devices/{deviceId}/onboarding/status")
    public DeviceOnboardingStatusSummary onboardingStatus(@PathVariable String deviceId) {
        return onboardingStatusService.statusForDevice(deviceId);
    }

    @PostMapping("/devices/{deviceId}/onboarding/retry")
    public BrandOnboardingSessionSummary retryOnboarding(
            @PathVariable String deviceId,
            @RequestParam(required = false) String brand
    ) {
        return candidateDiscoveryService.retryOnboardingForDevice(deviceId, brand);
    }

    @GetMapping("/discovery/candidates")
    public List<DiscoveryCandidateSummary> candidates(@RequestParam(required = false) io.github.koyan9.tvremote.domain.CandidateStatus status) {
        return candidateDiscoveryService.candidates(status);
    }

    @PostMapping("/discovery/candidates/scan")
    public List<DiscoveryCandidateSummary> scanCandidates() {
        return candidateDiscoveryService.scanCandidates();
    }

    @GetMapping("/discovery/candidates/{candidateId}/pairing-suggestions")
    public List<PairingSuggestion> pairingSuggestions(@PathVariable String candidateId) {
        return candidateDiscoveryService.pairingSuggestions(candidateId);
    }

    @PostMapping("/discovery/candidates/{candidateId}/adopt")
    public RemoteDevice adoptCandidate(@PathVariable String candidateId, @RequestBody CandidateAdoptionRequest request) {
        return candidateDiscoveryService.adoptCandidate(candidateId, request);
    }

    @PostMapping("/discovery/candidates/{candidateId}/dismiss")
    public DiscoveryCandidateSummary dismissCandidate(@PathVariable String candidateId) {
        return candidateDiscoveryService.dismissCandidate(candidateId);
    }

    @PostMapping("/discovery/candidates/{candidateId}/reopen")
    public DiscoveryCandidateSummary reopenCandidate(@PathVariable String candidateId) {
        return candidateDiscoveryService.reopenCandidate(candidateId);
    }

    @GetMapping("/devices")
    public List<RemoteDevice> devices() {
        return deviceCatalogService.televisionDevices();
    }

    @PostMapping("/devices/register")
    public RemoteDevice registerDevice(@Valid @RequestBody DeviceRegistrationRequest request) {
        return remoteManagementService.registerDevice(request);
    }

    @PostMapping("/pairings")
    public DevicePairingSummary createPairing(@Valid @RequestBody DevicePairingRequest request) {
        return pairingManagementService.createPairing(request);
    }

    @PatchMapping("/pairings/{pairingId}")
    public DevicePairingSummary updatePairing(@PathVariable String pairingId, @RequestBody DevicePairingUpdateRequest request) {
        return pairingManagementService.updatePairing(pairingId, request);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/pairings/{pairingId}")
    public void revokePairing(@PathVariable String pairingId) {
        pairingManagementService.revokePairing(pairingId);
    }

    @GetMapping("/devices/{deviceId}")
    public RemoteDevice device(@PathVariable String deviceId) {
        return deviceCatalogService.getDevice(deviceId);
    }

    @GetMapping("/executions")
    public List<CommandResult> executions() {
        return controlExecutionService.recentExecutions();
    }

    @PostMapping("/discovery/scan")
    public DiscoveryResult scan() {
        return discoveryService.scanHomeNetwork();
    }

    @PostMapping("/devices/{deviceId}/commands")
    public CommandResult sendCommand(@PathVariable String deviceId, @Valid @RequestBody CommandRequest request) {
        return controlExecutionService.execute(deviceId, request.command(), request.preferredGatewayId());
    }

    @GetMapping("/adapters")
    public List<AdapterDescriptor> adapters() {
        return brandAdapterRegistry.descriptors();
    }

    @GetMapping("/integrations")
    public Map<String, Object> integrations() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("defaultMode", remoteIntegrationProperties.modeFor("__default__"));
        payload.put("configuredAdapterModes", remoteIntegrationProperties.adapterModes());
        payload.put("registeredClients", protocolClientRegistry.descriptors());
        payload.put("samsungEndpoint", remoteIntegrationProperties.samsung().endpoint());
        payload.put("sonyEndpoint", remoteIntegrationProperties.sony().endpoint());
        payload.put("lgEndpoint", remoteIntegrationProperties.lg().endpoint());
        payload.put("gatewayEndpoint", remoteIntegrationProperties.gateway().endpoint());
        payload.put("gatewayInfraredEndpoint", remoteIntegrationProperties.gateway().infraredEndpoint());
        payload.put("gatewayHdmiCecEndpoint", remoteIntegrationProperties.gateway().hdmiCecEndpoint());
        payload.put("gatewayHubId", remoteIntegrationProperties.gateway().hubId());
        return payload;
    }

    @GetMapping("/profile")
    public Map<String, Object> profile() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("projectName", remoteProjectProperties.name());
        payload.put("standalone", remoteProjectProperties.standalone());
        payload.put("currentMode", remoteProjectProperties.currentMode());
        payload.put("targetClients", remoteProjectProperties.targetClients());
        payload.put("note", remoteProjectProperties.note());
        payload.put("controlPaths", List.of("LAN_DIRECT", "IR_GATEWAY", "HDMI_CEC_GATEWAY"));
        payload.put("strategy", "auto-route with direct LAN priority and gateway fallback");
        payload.put("brandAdapters", brandAdapterRegistry.descriptors());
        payload.put("protocolClients", protocolClientRegistry.descriptors());
        payload.put("defaultIntegrationMode", remoteIntegrationProperties.modeFor("__default__"));
        return payload;
    }
}


