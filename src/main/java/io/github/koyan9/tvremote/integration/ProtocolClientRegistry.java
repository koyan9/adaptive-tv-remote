package io.github.koyan9.tvremote.integration;

import io.github.koyan9.tvremote.brand.BrandDispatchPlan;
import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.ControlDecision;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProtocolClientRegistry {

    private final List<ProtocolClient> protocolClients;
    private final RemoteIntegrationProperties remoteIntegrationProperties;

    public ProtocolClientRegistry(List<ProtocolClient> protocolClients, RemoteIntegrationProperties remoteIntegrationProperties) {
        this.protocolClients = List.copyOf(protocolClients);
        this.remoteIntegrationProperties = remoteIntegrationProperties;
    }

    public ProtocolDispatchResult dispatch(
            RemoteDevice device,
            RemoteCommand command,
            ControlDecision decision,
            BrandDispatchPlan dispatchPlan
    ) {
        IntegrationMode desiredMode = remoteIntegrationProperties.modeFor(dispatchPlan.adapterKey());
        ProtocolClient client = resolveClient(dispatchPlan, desiredMode);
        return client.dispatch(device, command, decision, dispatchPlan);
    }

    public List<ProtocolClientDescriptor> descriptors() {
        return protocolClients.stream()
                .map(client -> new ProtocolClientDescriptor(client.clientKey(), client.integrationMode(), client.description()))
                .toList();
    }

    public IntegrationMode modeFor(String adapterKey) {
        return remoteIntegrationProperties.modeFor(adapterKey);
    }

    private ProtocolClient resolveClient(BrandDispatchPlan dispatchPlan, IntegrationMode desiredMode) {
        return protocolClients.stream()
                .filter(client -> client.integrationMode() == desiredMode)
                .filter(client -> client.supports(dispatchPlan))
                .findFirst()
                .orElseGet(() -> {
                    if (desiredMode != IntegrationMode.MOCK && remoteIntegrationProperties.strictMode()) {
                        throw new IntegrationConfigurationException("No " + desiredMode + " protocol client registered for adapter " + dispatchPlan.adapterKey());
                    }
                    return protocolClients.stream()
                            .filter(client -> client.integrationMode() == IntegrationMode.MOCK)
                            .filter(client -> client.supports(dispatchPlan))
                            .findFirst()
                            .orElseThrow(() -> new IntegrationConfigurationException("No protocol client registered for adapter " + dispatchPlan.adapterKey()));
                });
    }
}


