package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.adapter.ControlAdapter;
import io.github.koyan9.tvremote.brand.BrandAdapterRegistry;
import io.github.koyan9.tvremote.brand.BrandDispatchPlan;
import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.integration.ProtocolClientRegistry;
import io.github.koyan9.tvremote.integration.ProtocolDispatchResult;
import io.github.koyan9.tvremote.model.CommandResult;
import io.github.koyan9.tvremote.model.ControlDecision;
import io.github.koyan9.tvremote.model.RemoteDevice;
import io.github.koyan9.tvremote.persistence.CommandExecutionEntity;
import io.github.koyan9.tvremote.persistence.CommandExecutionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ControlExecutionService {

    private final DeviceCatalogService deviceCatalogService;
    private final ControlRoutingService controlRoutingService;
    private final BrandAdapterRegistry brandAdapterRegistry;
    private final ProtocolClientRegistry protocolClientRegistry;
    private final CommandExecutionRepository commandExecutionRepository;
    private final Map<ControlPath, ControlAdapter> adapters;

    public ControlExecutionService(
            DeviceCatalogService deviceCatalogService,
            ControlRoutingService controlRoutingService,
            BrandAdapterRegistry brandAdapterRegistry,
            ProtocolClientRegistry protocolClientRegistry,
            CommandExecutionRepository commandExecutionRepository,
            List<ControlAdapter> controlAdapters
    ) {
        this.deviceCatalogService = deviceCatalogService;
        this.controlRoutingService = controlRoutingService;
        this.brandAdapterRegistry = brandAdapterRegistry;
        this.protocolClientRegistry = protocolClientRegistry;
        this.commandExecutionRepository = commandExecutionRepository;
        this.adapters = controlAdapters.stream().collect(Collectors.toMap(ControlAdapter::path, Function.identity()));
    }

    public CommandResult execute(String deviceId, RemoteCommand command, String preferredGatewayId, String networkName) {
        RemoteDevice device = deviceCatalogService.getDevice(deviceId);
        if (!device.isTelevision()) {
            throw new IllegalArgumentException("Only television devices can receive remote commands.");
        }
        if (!device.capability().supports(command)) {
            throw new IllegalArgumentException("Command " + command + " is not supported for device " + device.displayName());
        }

        ControlDecision decision = controlRoutingService.chooseRoute(device, preferredGatewayId, networkName);
        ControlAdapter adapter = adapters.get(decision.path());
        if (adapter == null) {
            throw new IllegalStateException("No adapter registered for path " + decision.path());
        }

        BrandDispatchPlan dispatchPlan = brandAdapterRegistry.resolve(device, decision.path(), command);
        ProtocolDispatchResult protocolDispatchResult = protocolClientRegistry.dispatch(device, command, decision, dispatchPlan);
        CommandResult result = adapter.execute(device, command, decision, dispatchPlan, protocolDispatchResult);
        commandExecutionRepository.save(toEntity(result));
        return result;
    }

    public List<CommandResult> recentExecutions() {
        return commandExecutionRepository.findTop12ByOrderByExecutedAtDesc().stream()
                .map(this::toModel)
                .toList();
    }

    private CommandExecutionEntity toEntity(CommandResult result) {
        return new CommandExecutionEntity(
                result.correlationId(),
                result.deviceId(),
                result.deviceName(),
                result.command(),
                result.route(),
                result.gatewayDeviceId(),
                result.adapterLabel(),
                result.brandAdapterKey(),
                result.protocolFamily(),
                result.protocolClientKey(),
                result.integrationMode(),
                result.integrationEndpoint(),
                result.status(),
                result.message(),
                result.executedAt()
        );
    }

    private CommandResult toModel(CommandExecutionEntity entity) {
        return new CommandResult(
                entity.getId(),
                entity.getDeviceId(),
                entity.getDeviceName(),
                entity.getCommand(),
                entity.getRoute(),
                entity.getGatewayDeviceId(),
                entity.getAdapterLabel(),
                entity.getBrandAdapterKey(),
                entity.getProtocolFamily(),
                entity.getProtocolClientKey(),
                entity.getIntegrationMode(),
                entity.getIntegrationEndpoint(),
                entity.getStatus(),
                entity.getMessage(),
                entity.getExecutedAt()
        );
    }
}


