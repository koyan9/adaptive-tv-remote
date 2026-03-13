package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.brand.AdapterDescriptor;
import io.github.koyan9.tvremote.brand.BrandAdapterRegistry;
import io.github.koyan9.tvremote.brand.BrandDispatchPlan;
import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import io.github.koyan9.tvremote.integration.IntegrationMode;
import io.github.koyan9.tvremote.integration.ProtocolClient;
import io.github.koyan9.tvremote.model.AdapterHealthStatus;
import io.github.koyan9.tvremote.model.IntegrationHealthReport;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IntegrationHealthService {

    private final BrandAdapterRegistry brandAdapterRegistry;
    private final RemoteIntegrationProperties remoteIntegrationProperties;
    private final List<ProtocolClient> protocolClients;

    public IntegrationHealthService(
            BrandAdapterRegistry brandAdapterRegistry,
            RemoteIntegrationProperties remoteIntegrationProperties,
            List<ProtocolClient> protocolClients
    ) {
        this.brandAdapterRegistry = brandAdapterRegistry;
        this.remoteIntegrationProperties = remoteIntegrationProperties;
        this.protocolClients = List.copyOf(protocolClients);
    }

    public IntegrationHealthReport report() {
        List<AdapterHealthStatus> adapters = brandAdapterRegistry.descriptors().stream()
                .map(this::buildStatus)
                .toList();
        return new IntegrationHealthReport(
                remoteIntegrationProperties.defaultMode(),
                remoteIntegrationProperties.strictMode(),
                adapters
        );
    }

    private AdapterHealthStatus buildStatus(AdapterDescriptor adapter) {
        IntegrationMode desiredMode = remoteIntegrationProperties.modeFor(adapter.adapterKey());
        boolean enabled = isEnabled(adapter.adapterKey());
        List<String> missing = missingConfig(adapter.adapterKey());
        boolean configComplete = missing.isEmpty();

        BrandDispatchPlan plan = new BrandDispatchPlan(
                adapter.adapterKey(),
                adapter.brand(),
                adapter.protocolFamily(),
                "health-check"
        );
        List<String> availableClients = protocolClients.stream()
                .filter(client -> client.supports(plan))
                .map(client -> client.integrationMode() + ":" + client.clientKey())
                .toList();
        boolean clientAvailable = protocolClients.stream()
                .anyMatch(client -> client.integrationMode() == desiredMode && client.supports(plan));

        List<String> issues = new ArrayList<>();
        if (!enabled) {
            issues.add("disabled");
        }
        if (!configComplete) {
            issues.add("missing-config:" + String.join(",", missing));
        }
        if (!clientAvailable) {
            issues.add("no-client:" + desiredMode);
        }

        boolean ready = enabled && configComplete && clientAvailable;

        return new AdapterHealthStatus(
                adapter.adapterKey(),
                adapter.brand(),
                adapter.path(),
                desiredMode.name(),
                enabled,
                configComplete,
                clientAvailable,
                ready,
                missing,
                availableClients,
                issues
        );
    }

    private boolean isEnabled(String adapterKey) {
        if ("samsung-lan".equals(adapterKey)) {
            return remoteIntegrationProperties.samsung().enabled();
        }
        if ("lg-lan".equals(adapterKey)) {
            return remoteIntegrationProperties.lg().enabled();
        }
        if ("sony-lan".equals(adapterKey)) {
            return remoteIntegrationProperties.sony().enabled();
        }
        if ("generic-ir".equals(adapterKey) || "generic-hdmi-cec".equals(adapterKey)) {
            return remoteIntegrationProperties.gateway().enabled();
        }
        return true;
    }

    private List<String> missingConfig(String adapterKey) {
        List<String> missing = new ArrayList<>();
        if ("samsung-lan".equals(adapterKey)) {
            if (isBlank(remoteIntegrationProperties.samsung().endpoint())) {
                missing.add("samsung.endpoint");
            }
            if (isBlank(remoteIntegrationProperties.samsung().clientName())) {
                missing.add("samsung.clientName");
            }
        } else if ("lg-lan".equals(adapterKey)) {
            if (isBlank(remoteIntegrationProperties.lg().endpoint())) {
                missing.add("lg.endpoint");
            }
        } else if ("sony-lan".equals(adapterKey)) {
            if (isBlank(remoteIntegrationProperties.sony().endpoint())) {
                missing.add("sony.endpoint");
            }
            if (isBlank(remoteIntegrationProperties.sony().preSharedKey())) {
                missing.add("sony.preSharedKey");
            }
        } else if ("generic-ir".equals(adapterKey)) {
            RemoteIntegrationProperties.Gateway gateway = remoteIntegrationProperties.gateway();
            if (isBlank(gateway.infraredEndpoint())) {
                missing.add("gateway.infraredEndpoint");
            }
            if (isBlank(gateway.hubId())) {
                missing.add("gateway.hubId");
            }
            if (isBlank(gateway.authToken())) {
                missing.add("gateway.authToken");
            }
        } else if ("generic-hdmi-cec".equals(adapterKey)) {
            RemoteIntegrationProperties.Gateway gateway = remoteIntegrationProperties.gateway();
            if (isBlank(gateway.hdmiCecEndpoint())) {
                missing.add("gateway.hdmiCecEndpoint");
            }
            if (isBlank(gateway.hubId())) {
                missing.add("gateway.hubId");
            }
            if (isBlank(gateway.authToken())) {
                missing.add("gateway.authToken");
            }
        }
        return missing;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
