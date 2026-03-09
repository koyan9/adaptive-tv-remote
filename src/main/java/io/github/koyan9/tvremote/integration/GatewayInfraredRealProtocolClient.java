package io.github.koyan9.tvremote.integration;

import io.github.koyan9.tvremote.brand.BrandDispatchPlan;
import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.ControlDecision;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.springframework.stereotype.Component;

@Component
public class GatewayInfraredRealProtocolClient implements ProtocolClient {

    private final RemoteIntegrationProperties remoteIntegrationProperties;

    public GatewayInfraredRealProtocolClient(RemoteIntegrationProperties remoteIntegrationProperties) {
        this.remoteIntegrationProperties = remoteIntegrationProperties;
    }

    @Override
    public String clientKey() {
        return "gateway-ir-real-client";
    }

    @Override
    public IntegrationMode integrationMode() {
        return IntegrationMode.REAL;
    }

    @Override
    public String description() {
        return "Real-protocol skeleton for sending infrared commands through the home gateway API.";
    }

    @Override
    public boolean supports(BrandDispatchPlan dispatchPlan) {
        return "generic-ir".equals(dispatchPlan.adapterKey());
    }

    @Override
    public ProtocolDispatchResult dispatch(RemoteDevice device, RemoteCommand command, ControlDecision decision, BrandDispatchPlan dispatchPlan) {
        RemoteIntegrationProperties.Gateway gateway = remoteIntegrationProperties.gateway();
        if (!gateway.enabled()) {
            throw new IllegalStateException("Gateway infrared real integration is disabled.");
        }
        if (gateway.infraredEndpoint() == null || gateway.infraredEndpoint().isBlank()) {
            throw new IllegalStateException("Gateway infrared endpoint is missing.");
        }

        return new ProtocolDispatchResult(
                clientKey(),
                integrationMode(),
                gateway.infraredEndpoint(),
                "Prepared an infrared gateway request for " + command + " via hub " + gateway.hubId() + " using token " + gateway.authToken() + "."
        );
    }
}


