package io.github.koyan9.tvremote.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SamsungLanPayloadFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SamsungLanPayloadFactory payloadFactory = new SamsungLanPayloadFactory(objectMapper);

    @Test
    void createsEndpointWithClientNameAndTokenAndEncodesPayload() throws Exception {
        RemoteIntegrationProperties.Samsung samsung = new RemoteIntegrationProperties.Samsung(
                true,
                "ws://192.168.50.21:8001/api/v2/channels/samsung.remote.control",
                "demo-token",
                "Adaptive TV Remote"
        );

        SamsungLanCommandRequest request = payloadFactory.create(samsung, RemoteCommand.HOME);
        JsonNode payload = objectMapper.readTree(request.payload());

        assertThat(request.endpoint().toString())
                .contains("name=")
                .contains("token=demo-token");
        assertThat(request.remoteKey()).isEqualTo("KEY_HOME");
        assertThat(payload.get("method").asText()).isEqualTo("ms.remote.control");
        assertThat(payload.get("params").get("DataOfCmd").asText()).isEqualTo("KEY_HOME");
    }
}
