package io.github.koyan9.tvremote.integration;

import io.github.koyan9.tvremote.brand.BrandDispatchPlan;
import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.ControlDecision;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.springframework.stereotype.Component;

@Component
public class SonyLanRealProtocolClient implements ProtocolClient {

    private final RemoteIntegrationProperties remoteIntegrationProperties;

    public SonyLanRealProtocolClient(RemoteIntegrationProperties remoteIntegrationProperties) {
        this.remoteIntegrationProperties = remoteIntegrationProperties;
    }

    @Override
    public String clientKey() {
        return "sony-real-lan-client";
    }

    @Override
    public IntegrationMode integrationMode() {
        return IntegrationMode.REAL;
    }

    @Override
    public String description() {
        return "Real-protocol skeleton for Sony BRAVIA IP control with configurable endpoint and pre-shared key.";
    }

    @Override
    public boolean supports(BrandDispatchPlan dispatchPlan) {
        return "sony-lan".equals(dispatchPlan.adapterKey());
    }

    @Override
    public ProtocolDispatchResult dispatch(RemoteDevice device, RemoteCommand command, ControlDecision decision, BrandDispatchPlan dispatchPlan) {
        RemoteIntegrationProperties.Sony sony = remoteIntegrationProperties.sony();
        if (!sony.enabled()) {
            throw new IllegalStateException("Sony LAN real integration is disabled.");
        }
        if (sony.endpoint() == null || sony.endpoint().isBlank()) {
            throw new IllegalStateException("Sony LAN real integration endpoint is missing.");
        }

        return new ProtocolDispatchResult(
                clientKey(),
                integrationMode(),
                sony.endpoint(),
                "Prepared a Sony BRAVIA request skeleton for " + command + " using pre-shared key " + sony.preSharedKey() + "."
        );
    }
}


