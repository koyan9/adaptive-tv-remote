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

class GatewayHdmiCecPayloadFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GatewayHdmiCecPayloadFactory payloadFactory = new GatewayHdmiCecPayloadFactory(objectMapper);

    @Test
    void createsHdmiCecGatewayPayloadWithActionKey() throws Exception {
        RemoteIntegrationProperties.Gateway gateway = new RemoteIntegrationProperties.Gateway(
                true,
                "http://gateway.local",
                "http://gateway.local/api/infrared/send",
                "http://gateway.local/api/cec/send",
                "hub-one-demo",
                "demo-gateway-token"
        );
        RemoteDevice device = new RemoteDevice(
                "tv-cinema-room",
                "Cinema Room Display",
                DeviceType.LEGACY_TV,
                "BenQ",
                "W4000i",
                "Cinema Room",
                false,
                Set.of(ControlPath.HDMI_CEC_GATEWAY),
                List.of("gateway-home-hub"),
                new DeviceCapability(false, false, false, Set.of(RemoteCommand.HOME)),
                new ModelProfile("BenQ", "W4000i", "Cinema Room Display", List.of(ControlPath.HDMI_CEC_GATEWAY), "cec")
        );

        GatewayHdmiCecCommandRequest request = payloadFactory.create(gateway, device, RemoteCommand.HOME);
        JsonNode payload = objectMapper.readTree(request.payload());

        assertThat(request.endpoint().toString()).isEqualTo("http://gateway.local/api/cec/send");
        assertThat(request.hubId()).isEqualTo("hub-one-demo");
        assertThat(request.actionKey()).isEqualTo("cec-home");
        assertThat(payload.get("transport").asText()).isEqualTo("hdmi-cec");
        assertThat(payload.get("target").get("deviceId").asText()).isEqualTo("tv-cinema-room");
        assertThat(payload.get("command").get("actionKey").asText()).isEqualTo("cec-home");
    }
}
