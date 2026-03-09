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
    private final SamsungLanPayloadFactory samsungLanPayloadFactory;
    private final SamsungLanSessionClient samsungLanSessionClient;

    public SamsungLanRealProtocolClient(
            RemoteIntegrationProperties remoteIntegrationProperties,
            SamsungLanPayloadFactory samsungLanPayloadFactory,
            SamsungLanSessionClient samsungLanSessionClient
    ) {
        this.remoteIntegrationProperties = remoteIntegrationProperties;
        this.samsungLanPayloadFactory = samsungLanPayloadFactory;
        this.samsungLanSessionClient = samsungLanSessionClient;
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
        return "First real-request Samsung LAN client using WebSocket command encoding with configurable endpoint, token, and client name.";
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
        if (samsung.clientName() == null || samsung.clientName().isBlank()) {
            throw new IllegalStateException("Samsung LAN client name is missing.");
        }

        SamsungLanCommandRequest request = samsungLanPayloadFactory.create(samsung, command);
        SamsungLanSessionResult sessionResult = samsungLanSessionClient.sendCommand(request);

        return new ProtocolDispatchResult(
                clientKey(),
                integrationMode(),
                sessionResult.endpoint().toString(),
                sessionResult.detail()
        );
    }
}


