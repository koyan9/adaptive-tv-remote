package io.github.koyan9.tvremote.integration;

import io.github.koyan9.tvremote.brand.BrandDispatchPlan;
import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.DeviceType;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.ControlDecision;
import io.github.koyan9.tvremote.model.DeviceCapability;
import io.github.koyan9.tvremote.model.ModelProfile;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProtocolClientRegistryStrictModeTest {

    @Test
    void failsWhenStrictModeEnabledAndNoRealClient() {
        RemoteIntegrationProperties properties = new RemoteIntegrationProperties(
                "real",
                Map.of("mock-only", "real"),
                true,
                null,
                null,
                null,
                null
        );

        ProtocolClientRegistry registry = new ProtocolClientRegistry(List.of(new MockProtocolClient()), properties);
        RemoteDevice device = new RemoteDevice(
                "tv-mock",
                "Mock TV",
                DeviceType.SMART_TV,
                "Mock",
                "Mock",
                "Room",
                true,
                Set.of(ControlPath.LAN_DIRECT),
                List.of(),
                new DeviceCapability(false, false, false, Set.of(RemoteCommand.HOME)),
                new ModelProfile("Mock", "Mock", "Mock TV", List.of(ControlPath.LAN_DIRECT), "mock")
        );
        ControlDecision decision = new ControlDecision(
                ControlPath.LAN_DIRECT,
                null,
                "LAN",
                List.of(ControlPath.LAN_DIRECT),
                "test"
        );
        BrandDispatchPlan plan = new BrandDispatchPlan("mock-only", "Mock", "Mock", "test");

        assertThatThrownBy(() -> registry.dispatch(device, RemoteCommand.HOME, decision, plan))
                .isInstanceOf(IntegrationConfigurationException.class)
                .hasMessageContaining("No REAL protocol client registered for adapter mock-only");
    }
}
