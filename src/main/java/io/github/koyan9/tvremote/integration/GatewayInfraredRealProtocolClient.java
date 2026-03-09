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
    private final GatewayInfraredPayloadFactory gatewayInfraredPayloadFactory;
    private final GatewayInfraredSessionClient gatewayInfraredSessionClient;

    public GatewayInfraredRealProtocolClient(
            RemoteIntegrationProperties remoteIntegrationProperties,
            GatewayInfraredPayloadFactory gatewayInfraredPayloadFactory,
            GatewayInfraredSessionClient gatewayInfraredSessionClient
    ) {
        this.remoteIntegrationProperties = remoteIntegrationProperties;
        this.gatewayInfraredPayloadFactory = gatewayInfraredPayloadFactory;
        this.gatewayInfraredSessionClient = gatewayInfraredSessionClient;
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
        return "First real-request infrared gateway client using HTTP command payloads with hub and token headers.";
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
        if (gateway.hubId() == null || gateway.hubId().isBlank()) {
            throw new IllegalStateException("Gateway hub ID is missing.");
        }
        if (gateway.authToken() == null || gateway.authToken().isBlank()) {
            throw new IllegalStateException("Gateway auth token is missing.");
        }

        GatewayInfraredCommandRequest request = gatewayInfraredPayloadFactory.create(gateway, device, command);
        GatewayInfraredSessionResult sessionResult = gatewayInfraredSessionClient.sendCommand(request);

        return new ProtocolDispatchResult(
                clientKey(),
                integrationMode(),
                sessionResult.endpoint().toString(),
                sessionResult.detail()
        );
    }
}


