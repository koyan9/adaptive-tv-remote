package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.DeviceType;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.DeviceRegistrationRequest;
import io.github.koyan9.tvremote.model.ControlDecision;
import io.github.koyan9.tvremote.persistence.HouseholdRepository;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ControlRoutingServiceTest {

    @Autowired
    private DeviceCatalogService deviceCatalogService;

    @Autowired
    private ControlRoutingService controlRoutingService;

    @Autowired
    private RemoteManagementService remoteManagementService;

    @Autowired
    private HouseholdRepository householdRepository;

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

    @Test
    @Transactional
    void attemptsLanDirectWhenWakeOnLanSupported() {
        String householdId = householdRepository.findFirstByOrderBySortOrderAsc()
                .orElseThrow()
                .getId();

        RemoteDevice device = remoteManagementService.registerDevice(new DeviceRegistrationRequest(
                "tv-wol",
                "Wake-on-LAN TV",
                DeviceType.SMART_TV,
                "TestBrand",
                "WOL-1",
                householdId,
                null,
                "Lab",
                false,
                java.util.Set.of(ControlPath.LAN_DIRECT),
                java.util.List.of(),
                true,
                false,
                true,
                java.util.Set.of(RemoteCommand.HOME),
                "WOL TV",
                java.util.List.of(ControlPath.LAN_DIRECT),
                "Offline but supports Wake-on-LAN."
        ));

        ControlDecision decision = controlRoutingService.chooseRoute(device, null);

        assertThat(decision.path()).isEqualTo(ControlPath.LAN_DIRECT);
        assertThat(decision.gatewayDeviceId()).isNull();
    }

    @Test
    @Transactional
    void blocksRoutesWhenPairingRequiredAndMissing() {
        String householdId = householdRepository.findFirstByOrderBySortOrderAsc()
                .orElseThrow()
                .getId();

        RemoteDevice device = remoteManagementService.registerDevice(new DeviceRegistrationRequest(
                "tv-pairing-required",
                "Pairing Required TV",
                DeviceType.SMART_TV,
                "TestBrand",
                "T-1000",
                householdId,
                null,
                "Lab",
                true,
                java.util.Set.of(ControlPath.LAN_DIRECT),
                java.util.List.of(),
                false,
                true,
                false,
                java.util.Set.of(RemoteCommand.HOME),
                "Test TV",
                java.util.List.of(ControlPath.LAN_DIRECT),
                "Requires pairing before routing."
        ));

        assertThatThrownBy(() -> controlRoutingService.chooseRoute(device, null))
                .isInstanceOf(ControlRoutingException.class);
    }
}


