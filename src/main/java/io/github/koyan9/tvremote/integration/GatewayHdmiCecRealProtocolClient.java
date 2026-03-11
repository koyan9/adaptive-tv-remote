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
    private final GatewayHdmiCecPayloadFactory gatewayHdmiCecPayloadFactory;
    private final GatewayHdmiCecSessionClient gatewayHdmiCecSessionClient;

    public GatewayHdmiCecRealProtocolClient(
            RemoteIntegrationProperties remoteIntegrationProperties,
            GatewayHdmiCecPayloadFactory gatewayHdmiCecPayloadFactory,
            GatewayHdmiCecSessionClient gatewayHdmiCecSessionClient
    ) {
        this.remoteIntegrationProperties = remoteIntegrationProperties;
        this.gatewayHdmiCecPayloadFactory = gatewayHdmiCecPayloadFactory;
        this.gatewayHdmiCecSessionClient = gatewayHdmiCecSessionClient;
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
        return "First real-request HDMI-CEC gateway client using HTTP action payloads with hub and token headers.";
    }

    @Override
    public boolean supports(BrandDispatchPlan dispatchPlan) {
        return "generic-hdmi-cec".equals(dispatchPlan.adapterKey());
    }

    @Override
    public ProtocolDispatchResult dispatch(RemoteDevice device, RemoteCommand command, ControlDecision decision, BrandDispatchPlan dispatchPlan) {
        RemoteIntegrationProperties.Gateway gateway = remoteIntegrationProperties.gateway();
        if (!gateway.enabled()) {
            throw new IntegrationDisabledException("Gateway HDMI-CEC real integration is disabled.");
        }
        if (gateway.hdmiCecEndpoint() == null || gateway.hdmiCecEndpoint().isBlank()) {
            throw new IntegrationConfigurationException("Gateway HDMI-CEC endpoint is missing.");
        }
        if (gateway.hubId() == null || gateway.hubId().isBlank()) {
            throw new IntegrationConfigurationException("Gateway hub ID is missing.");
        }
        if (gateway.authToken() == null || gateway.authToken().isBlank()) {
            throw new IntegrationConfigurationException("Gateway auth token is missing.");
        }

        GatewayHdmiCecCommandRequest request = gatewayHdmiCecPayloadFactory.create(gateway, device, command);
        GatewayHdmiCecSessionResult sessionResult = gatewayHdmiCecSessionClient.sendCommand(request);

        return new ProtocolDispatchResult(
                clientKey(),
                integrationMode(),
                sessionResult.endpoint().toString(),
                sessionResult.detail()
        );
    }
}


