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
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
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
    private final Map<ControlPath, ControlAdapter> adapters;
    private final Deque<CommandResult> recentExecutions = new ArrayDeque<>();
    private final Object executionLock = new Object();

    public ControlExecutionService(
            DeviceCatalogService deviceCatalogService,
            ControlRoutingService controlRoutingService,
            BrandAdapterRegistry brandAdapterRegistry,
            ProtocolClientRegistry protocolClientRegistry,
            List<ControlAdapter> controlAdapters
    ) {
        this.deviceCatalogService = deviceCatalogService;
        this.controlRoutingService = controlRoutingService;
        this.brandAdapterRegistry = brandAdapterRegistry;
        this.protocolClientRegistry = protocolClientRegistry;
        this.adapters = controlAdapters.stream().collect(Collectors.toMap(ControlAdapter::path, Function.identity()));
    }

    public CommandResult execute(String deviceId, RemoteCommand command, String preferredGatewayId) {
        RemoteDevice device = deviceCatalogService.getDevice(deviceId);
        if (!device.isTelevision()) {
            throw new IllegalArgumentException("Only television devices can receive remote commands.");
        }
        if (!device.capability().supports(command)) {
            throw new IllegalArgumentException("Command " + command + " is not supported for device " + device.displayName());
        }

        ControlDecision decision = controlRoutingService.chooseRoute(device, preferredGatewayId);
        ControlAdapter adapter = adapters.get(decision.path());
        if (adapter == null) {
            throw new IllegalStateException("No adapter registered for path " + decision.path());
        }

        BrandDispatchPlan dispatchPlan = brandAdapterRegistry.resolve(device, decision.path(), command);
        ProtocolDispatchResult protocolDispatchResult = protocolClientRegistry.dispatch(device, command, decision, dispatchPlan);
        CommandResult result = adapter.execute(device, command, decision, dispatchPlan, protocolDispatchResult);
        remember(result);
        return result;
    }

    public List<CommandResult> recentExecutions() {
        synchronized (executionLock) {
            return List.copyOf(recentExecutions);
        }
    }

    private void remember(CommandResult result) {
        synchronized (executionLock) {
            recentExecutions.addFirst(result);
            while (recentExecutions.size() > 12) {
                recentExecutions.removeLast();
            }
        }
    }
}


