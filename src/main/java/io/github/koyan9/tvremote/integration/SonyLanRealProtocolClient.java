package io.github.koyan9.tvremote.integration;

import io.github.koyan9.tvremote.brand.BrandDispatchPlan;
import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.ControlDecision;
import io.github.koyan9.tvremote.model.RemoteDevice;
import io.github.koyan9.tvremote.service.BrandOnboardingRegistry;
import org.springframework.stereotype.Component;

@Component
public class SonyLanRealProtocolClient implements ProtocolClient {

    private final RemoteIntegrationProperties remoteIntegrationProperties;
    private final BrandOnboardingRegistry brandOnboardingRegistry;
    private final SonyIrccPayloadFactory sonyIrccPayloadFactory;
    private final SonyLanSessionClient sonyLanSessionClient;

    public SonyLanRealProtocolClient(
            RemoteIntegrationProperties remoteIntegrationProperties,
            BrandOnboardingRegistry brandOnboardingRegistry,
            SonyIrccPayloadFactory sonyIrccPayloadFactory,
            SonyLanSessionClient sonyLanSessionClient
    ) {
        this.remoteIntegrationProperties = remoteIntegrationProperties;
        this.brandOnboardingRegistry = brandOnboardingRegistry;
        this.sonyIrccPayloadFactory = sonyIrccPayloadFactory;
        this.sonyLanSessionClient = sonyLanSessionClient;
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
        return "Sony BRAVIA IP control over IRCC with configurable endpoint and pre-shared key.";
    }

    @Override
    public boolean supports(BrandDispatchPlan dispatchPlan) {
        return "sony-lan".equals(dispatchPlan.adapterKey());
    }

    @Override
    public ProtocolDispatchResult dispatch(RemoteDevice device, RemoteCommand command, ControlDecision decision, BrandDispatchPlan dispatchPlan) {
        RemoteIntegrationProperties.Sony configuredSony = remoteIntegrationProperties.sony();
        String negotiatedPreSharedKey = brandOnboardingRegistry.latestNegotiatedCredential(device.id(), "Sony");
        RemoteIntegrationProperties.Sony sony = new RemoteIntegrationProperties.Sony(
                configuredSony.enabled(),
                configuredSony.endpoint(),
                negotiatedPreSharedKey != null ? negotiatedPreSharedKey : configuredSony.preSharedKey(),
                configuredSony.irccEndpoint()
        );
        if (!sony.enabled()) {
            throw new IntegrationDisabledException("Sony LAN real integration is disabled.");
        }
        if (sony.endpoint() == null || sony.endpoint().isBlank()) {
            throw new IntegrationConfigurationException("Sony LAN real integration endpoint is missing.");
        }
        if (sony.preSharedKey() == null || sony.preSharedKey().isBlank()) {
            throw new IntegrationConfigurationException("Sony LAN pre-shared key is missing.");
        }

        var irccEndpoint = SonyLanEndpoints.resolveIrccEndpoint(sony);
        SonyIrccCommandRequest request = sonyIrccPayloadFactory.create(irccEndpoint.toString(), sony.preSharedKey(), command);
        SonyLanSessionResult result = sonyLanSessionClient.sendCommand(request);

        return new ProtocolDispatchResult(
                clientKey(),
                integrationMode(),
                result.endpoint().toString(),
                result.detail()
        );
    }
}


