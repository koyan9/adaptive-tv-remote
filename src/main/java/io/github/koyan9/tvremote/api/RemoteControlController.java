package io.github.koyan9.tvremote.api;

import io.github.koyan9.tvremote.brand.AdapterDescriptor;
import io.github.koyan9.tvremote.brand.BrandAdapterRegistry;
import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import io.github.koyan9.tvremote.config.RemoteProjectProperties;
import io.github.koyan9.tvremote.integration.ProtocolClientRegistry;
import io.github.koyan9.tvremote.model.CommandRequest;
import io.github.koyan9.tvremote.model.CommandResult;
import io.github.koyan9.tvremote.model.DiscoveryResult;
import io.github.koyan9.tvremote.model.RemoteDevice;
import io.github.koyan9.tvremote.service.ControlExecutionService;
import io.github.koyan9.tvremote.service.DeviceCatalogService;
import io.github.koyan9.tvremote.service.DiscoveryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public RemoteControlController(
            DeviceCatalogService deviceCatalogService,
            DiscoveryService discoveryService,
            ControlExecutionService controlExecutionService,
            BrandAdapterRegistry brandAdapterRegistry,
            RemoteProjectProperties remoteProjectProperties,
            ProtocolClientRegistry protocolClientRegistry,
            RemoteIntegrationProperties remoteIntegrationProperties
    ) {
        this.deviceCatalogService = deviceCatalogService;
        this.discoveryService = discoveryService;
        this.controlExecutionService = controlExecutionService;
        this.brandAdapterRegistry = brandAdapterRegistry;
        this.remoteProjectProperties = remoteProjectProperties;
        this.protocolClientRegistry = protocolClientRegistry;
        this.remoteIntegrationProperties = remoteIntegrationProperties;
    }

    @GetMapping("/devices")
    public List<RemoteDevice> devices() {
        return deviceCatalogService.televisionDevices();
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
        return Map.of(
                "defaultMode", remoteIntegrationProperties.modeFor("__default__"),
                "configuredAdapterModes", remoteIntegrationProperties.adapterModes(),
                "registeredClients", protocolClientRegistry.descriptors(),
                "samsungEndpoint", remoteIntegrationProperties.samsung().endpoint(),
                "sonyEndpoint", remoteIntegrationProperties.sony().endpoint(),
                "lgEndpoint", remoteIntegrationProperties.lg().endpoint(),
                "gatewayEndpoint", remoteIntegrationProperties.gateway().endpoint(),
                "gatewayInfraredEndpoint", remoteIntegrationProperties.gateway().infraredEndpoint(),
                "gatewayHdmiCecEndpoint", remoteIntegrationProperties.gateway().hdmiCecEndpoint(),
                "gatewayHubId", remoteIntegrationProperties.gateway().hubId()
        );
    }

    @GetMapping("/profile")
    public Map<String, Object> profile() {
        return Map.of(
                "projectName", remoteProjectProperties.name(),
                "standalone", remoteProjectProperties.standalone(),
                "currentMode", remoteProjectProperties.currentMode(),
                "targetClients", remoteProjectProperties.targetClients(),
                "note", remoteProjectProperties.note(),
                "controlPaths", List.of("LAN_DIRECT", "IR_GATEWAY", "HDMI_CEC_GATEWAY"),
                "strategy", "auto-route with direct LAN priority and gateway fallback",
                "brandAdapters", brandAdapterRegistry.descriptors(),
                "protocolClients", protocolClientRegistry.descriptors(),
                "defaultIntegrationMode", remoteIntegrationProperties.modeFor("__default__")
        );
    }
}


