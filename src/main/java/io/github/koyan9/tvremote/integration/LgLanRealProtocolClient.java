package io.github.koyan9.tvremote.integration;

import io.github.koyan9.tvremote.brand.BrandDispatchPlan;
import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.ControlDecision;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.springframework.stereotype.Component;

@Component
public class LgLanRealProtocolClient implements ProtocolClient {

    private final RemoteIntegrationProperties remoteIntegrationProperties;

    public LgLanRealProtocolClient(RemoteIntegrationProperties remoteIntegrationProperties) {
        this.remoteIntegrationProperties = remoteIntegrationProperties;
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
        RemoteIntegrationProperties.Lg lg = remoteIntegrationProperties.lg();
        if (!lg.enabled()) {
            throw new IllegalStateException("LG LAN real integration is disabled.");
        }
        if (lg.endpoint() == null || lg.endpoint().isBlank()) {
            throw new IllegalStateException("LG LAN real integration endpoint is missing.");
        }

        return new ProtocolDispatchResult(
                clientKey(),
                integrationMode(),
                lg.endpoint(),
                "Prepared an LG webOS request skeleton for " + command + " using client key " + lg.clientKey() + "."
        );
    }
}


