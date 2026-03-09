package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.model.ControlDecision;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ControlRoutingServiceTest {

    private DeviceCatalogService deviceCatalogService;
    private ControlRoutingService controlRoutingService;

    @BeforeEach
    void setUp() {
        deviceCatalogService = new DeviceCatalogService(new ModelProfileService());
        controlRoutingService = new ControlRoutingService(deviceCatalogService);
    }

    @Test
    void choosesLanDirectForOnlineSmartTelevision() {
        RemoteDevice device = deviceCatalogService.getDevice("tv-living-room");

        ControlDecision decision = controlRoutingService.chooseRoute(device, null);

        assertThat(decision.path()).isEqualTo(ControlPath.LAN_DIRECT);
        assertThat(decision.gatewayDeviceId()).isNull();
    }

    @Test
    void fallsBackToInfraredGatewayWhenDirectControlIsOffline() {
        RemoteDevice device = deviceCatalogService.getDevice("tv-bedroom");

        ControlDecision decision = controlRoutingService.chooseRoute(device, null);

        assertThat(decision.path()).isEqualTo(ControlPath.IR_GATEWAY);
        assertThat(decision.gatewayDeviceId()).isEqualTo("gateway-home-hub");
    }

    @Test
    void usesInfraredForLegacyTelevision() {
        RemoteDevice device = deviceCatalogService.getDevice("tv-guest-room");

        ControlDecision decision = controlRoutingService.chooseRoute(device, null);

        assertThat(decision.path()).isEqualTo(ControlPath.IR_GATEWAY);
        assertThat(decision.adapterLabel()).contains("Infrared");
    }
}


