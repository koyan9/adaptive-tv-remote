package io.github.koyan9.tvremote.api;

import io.github.koyan9.tvremote.brand.AdapterDescriptor;
import io.github.koyan9.tvremote.brand.BrandAdapterRegistry;
import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import io.github.koyan9.tvremote.config.RemoteProjectProperties;
import io.github.koyan9.tvremote.integration.ProtocolClientRegistry;
import io.github.koyan9.tvremote.model.CommandRequest;
import io.github.koyan9.tvremote.model.CommandResult;
import io.github.koyan9.tvremote.model.DeviceRegistrationRequest;
import io.github.koyan9.tvremote.model.DiscoveryResult;
import io.github.koyan9.tvremote.model.HouseholdSummary;
import io.github.koyan9.tvremote.model.RemoteDevice;
import io.github.koyan9.tvremote.model.RoomSummary;
import io.github.koyan9.tvremote.service.ControlExecutionService;
import io.github.koyan9.tvremote.service.DeviceCatalogService;
import io.github.koyan9.tvremote.service.DiscoveryService;
import io.github.koyan9.tvremote.service.RemoteManagementService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final BrandAdapterRegistry brandAdapterRegistry;
    private final RemoteProjectProperties remoteProjectProperties;
    private final ProtocolClientRegistry protocolClientRegistry;
    private final RemoteIntegrationProperties remoteIntegrationProperties;
    private final RemoteManagementService remoteManagementService;

    public RemoteControlController(
            DeviceCatalogService deviceCatalogService,
            DiscoveryService discoveryService,
            ControlExecutionService controlExecutionService,
            BrandAdapterRegistry brandAdapterRegistry,
            RemoteProjectProperties remoteProjectProperties,
            ProtocolClientRegistry protocolClientRegistry,
            RemoteIntegrationProperties remoteIntegrationProperties,
            RemoteManagementService remoteManagementService
    ) {
        this.deviceCatalogService = deviceCatalogService;
        this.discoveryService = discoveryService;
        this.controlExecutionService = controlExecutionService;
        this.brandAdapterRegistry = brandAdapterRegistry;
        this.remoteProjectProperties = remoteProjectProperties;
        this.protocolClientRegistry = protocolClientRegistry;
        this.remoteIntegrationProperties = remoteIntegrationProperties;
        this.remoteManagementService = remoteManagementService;
    }

    @GetMapping("/households")
    public List<HouseholdSummary> households() {
        return remoteManagementService.households();
    }

    @GetMapping("/rooms")
    public List<RoomSummary> rooms(@RequestParam(required = false) String householdId) {
        return remoteManagementService.rooms(householdId);
    }

    @GetMapping("/devices")
    public List<RemoteDevice> devices() {
        return deviceCatalogService.televisionDevices();
    }

    @PostMapping("/devices/register")
    public RemoteDevice registerDevice(@Valid @RequestBody DeviceRegistrationRequest request) {
        return remoteManagementService.registerDevice(request);
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


