package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.model.ControlDecision;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ControlRoutingServiceTest {

    @Autowired
    private DeviceCatalogService deviceCatalogService;

    @Autowired
    private ControlRoutingService controlRoutingService;

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


