package io.github.koyan9.tvremote.integration;

import io.github.koyan9.tvremote.brand.BrandDispatchPlan;
import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.ControlDecision;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.springframework.stereotype.Component;

@Component
public class GatewayHdmiCecRealProtocolClient implements ProtocolClient {

    private final RemoteIntegrationProperties remoteIntegrationProperties;

    public GatewayHdmiCecRealProtocolClient(RemoteIntegrationProperties remoteIntegrationProperties) {
        this.remoteIntegrationProperties = remoteIntegrationProperties;
    }

    @Override
    public String clientKey() {
        return "gateway-hdmi-cec-real-client";
    }

    @Override
    public IntegrationMode integrationMode() {
        return IntegrationMode.REAL;
    }

    @Override
    public String description() {
        return "Real-protocol skeleton for sending HDMI-CEC actions through the home gateway API.";
    }

    @Override
    public boolean supports(BrandDispatchPlan dispatchPlan) {
        return "generic-hdmi-cec".equals(dispatchPlan.adapterKey());
    }

    @Override
    public ProtocolDispatchResult dispatch(RemoteDevice device, RemoteCommand command, ControlDecision decision, BrandDispatchPlan dispatchPlan) {
        RemoteIntegrationProperties.Gateway gateway = remoteIntegrationProperties.gateway();
        if (!gateway.enabled()) {
            throw new IllegalStateException("Gateway HDMI-CEC real integration is disabled.");
        }
        if (gateway.hdmiCecEndpoint() == null || gateway.hdmiCecEndpoint().isBlank()) {
            throw new IllegalStateException("Gateway HDMI-CEC endpoint is missing.");
        }

        return new ProtocolDispatchResult(
                clientKey(),
                integrationMode(),
                gateway.hdmiCecEndpoint(),
                "Prepared an HDMI-CEC gateway request for " + command + " via hub " + gateway.hubId() + " using token " + gateway.authToken() + "."
        );
    }
}


