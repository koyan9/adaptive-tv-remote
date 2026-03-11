package io.github.koyan9.tvremote.integration;

import io.github.koyan9.tvremote.brand.BrandDispatchPlan;
import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.ControlDecision;
import io.github.koyan9.tvremote.model.RemoteDevice;
import io.github.koyan9.tvremote.service.BrandOnboardingRegistry;
import org.springframework.stereotype.Component;

@Component
public class LgLanRealProtocolClient implements ProtocolClient {

    private final RemoteIntegrationProperties remoteIntegrationProperties;
    private final BrandOnboardingRegistry brandOnboardingRegistry;

    public LgLanRealProtocolClient(
            RemoteIntegrationProperties remoteIntegrationProperties,
            BrandOnboardingRegistry brandOnboardingRegistry
    ) {
        this.remoteIntegrationProperties = remoteIntegrationProperties;
        this.brandOnboardingRegistry = brandOnboardingRegistry;
    }

    @Override
    public String clientKey() {
        return "lg-real-lan-client";
    }

    @Override
    public IntegrationMode integrationMode() {
        return IntegrationMode.REAL;
    }

    @Override
    public String description() {
        return "Real-protocol skeleton for LG webOS TV LAN control with configurable endpoint and client key.";
    }

    @Override
    public boolean supports(BrandDispatchPlan dispatchPlan) {
        return "lg-lan".equals(dispatchPlan.adapterKey());
    }

    @Override
    public ProtocolDispatchResult dispatch(RemoteDevice device, RemoteCommand command, ControlDecision decision, BrandDispatchPlan dispatchPlan) {
        RemoteIntegrationProperties.Lg configuredLg = remoteIntegrationProperties.lg();
        String negotiatedClientKey = brandOnboardingRegistry.latestNegotiatedCredential(device.id(), "LG");
        RemoteIntegrationProperties.Lg lg = new RemoteIntegrationProperties.Lg(
                configuredLg.enabled(),
                configuredLg.endpoint(),
                negotiatedClientKey != null ? negotiatedClientKey : configuredLg.clientKey()
        );
        if (!lg.enabled()) {
            throw new IntegrationDisabledException("LG LAN real integration is disabled.");
        }
        if (lg.endpoint() == null || lg.endpoint().isBlank()) {
            throw new IntegrationConfigurationException("LG LAN real integration endpoint is missing.");
        }

        return new ProtocolDispatchResult(
                clientKey(),
                integrationMode(),
                lg.endpoint(),
                "Prepared an LG webOS request skeleton for " + command + " using client key " + lg.clientKey() + "."
        );
    }
}


