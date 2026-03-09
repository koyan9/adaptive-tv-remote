package io.github.koyan9.tvremote.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.DeviceType;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.DeviceCapability;
import io.github.koyan9.tvremote.model.ModelProfile;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayInfraredPayloadFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GatewayInfraredPayloadFactory payloadFactory = new GatewayInfraredPayloadFactory(objectMapper);

    @Test
    void createsInfraredGatewayPayloadWithProfileKey() throws Exception {
        RemoteIntegrationProperties.Gateway gateway = new RemoteIntegrationProperties.Gateway(
                true,
                "http://gateway.local",
                "http://gateway.local/api/infrared/send",
                "http://gateway.local/api/cec/send",
                "hub-one-demo",
                "demo-gateway-token"
        );
        RemoteDevice device = new RemoteDevice(
                "tv-guest-room",
                "Guest Room Legacy TV",
                DeviceType.LEGACY_TV,
                "Hisense",
                "H32A4",
                "Guest Room",
                false,
                Set.of(ControlPath.IR_GATEWAY),
                List.of("gateway-home-hub"),
                new DeviceCapability(false, false, false, Set.of(RemoteCommand.HOME)),
                new ModelProfile("Hisense", "H32A4", "Guest Room Legacy TV", List.of(ControlPath.IR_GATEWAY), "legacy")
        );

        GatewayInfraredCommandRequest request = payloadFactory.create(gateway, device, RemoteCommand.HOME);
        JsonNode payload = objectMapper.readTree(request.payload());

        assertThat(request.endpoint().toString()).isEqualTo("http://gateway.local/api/infrared/send");
        assertThat(request.hubId()).isEqualTo("hub-one-demo");
        assertThat(request.profileKey()).isEqualTo("hisense-h32a4-home");
        assertThat(payload.get("transport").asText()).isEqualTo("infrared");
        assertThat(payload.get("target").get("deviceId").asText()).isEqualTo("tv-guest-room");
        assertThat(payload.get("command").get("profileKey").asText()).isEqualTo("hisense-h32a4-home");
    }
}
