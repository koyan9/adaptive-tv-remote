package io.github.koyan9.tvremote.integration;

import io.github.koyan9.tvremote.brand.BrandDispatchPlan;
import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.ControlDecision;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.springframework.stereotype.Component;

@Component
public class SamsungLanRealProtocolClient implements ProtocolClient {

    private final RemoteIntegrationProperties remoteIntegrationProperties;

    public SamsungLanRealProtocolClient(RemoteIntegrationProperties remoteIntegrationProperties) {
        this.remoteIntegrationProperties = remoteIntegrationProperties;
    }

    @Override
    public String clientKey() {
        return "samsung-real-lan-client";
    }

    @Override
    public IntegrationMode integrationMode() {
        return IntegrationMode.REAL;
    }

    @Override
    public String description() {
        return "Real-protocol skeleton for Samsung LAN control with configurable endpoint and token.";
    }

    @Override
    public boolean supports(BrandDispatchPlan dispatchPlan) {
        return "samsung-lan".equals(dispatchPlan.adapterKey());
    }

    @Override
    public ProtocolDispatchResult dispatch(RemoteDevice device, RemoteCommand command, ControlDecision decision, BrandDispatchPlan dispatchPlan) {
        RemoteIntegrationProperties.Samsung samsung = remoteIntegrationProperties.samsung();
        if (!samsung.enabled()) {
            throw new IllegalStateException("Samsung LAN real integration is disabled.");
        }
        if (samsung.endpoint() == null || samsung.endpoint().isBlank()) {
            throw new IllegalStateException("Samsung LAN real integration endpoint is missing.");
        }

        return new ProtocolDispatchResult(
                clientKey(),
                integrationMode(),
                samsung.endpoint(),
                "Prepared a Samsung LAN request skeleton for " + command + " using token " + samsung.token() + "."
        );
    }
}


