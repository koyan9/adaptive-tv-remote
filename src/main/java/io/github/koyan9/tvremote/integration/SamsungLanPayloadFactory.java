package io.github.koyan9.tvremote.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

@Component
public class SamsungLanPayloadFactory {

    private static final Map<RemoteCommand, String> REMOTE_KEYS = remoteKeys();

    private final ObjectMapper objectMapper;

    public SamsungLanPayloadFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SamsungLanCommandRequest create(RemoteIntegrationProperties.Samsung samsung, RemoteCommand command) {
        String remoteKey = REMOTE_KEYS.get(command);
        if (remoteKey == null) {
            throw new IllegalArgumentException("Samsung LAN does not support command " + command + " in the first real integration version.");
        }

        URI endpoint = buildEndpoint(samsung);
        String payload = buildPayload(remoteKey);
        return new SamsungLanCommandRequest(endpoint, payload, remoteKey);
    }

    private URI buildEndpoint(RemoteIntegrationProperties.Samsung samsung) {
        String baseEndpoint = samsung.endpoint();
        String encodedName = Base64.getEncoder().encodeToString(samsung.clientName().getBytes(StandardCharsets.UTF_8));

        StringBuilder query = new StringBuilder();
        query.append("name=").append(URLEncoder.encode(encodedName, StandardCharsets.UTF_8));
        if (samsung.token() != null && !samsung.token().isBlank()) {
            query.append("&token=").append(URLEncoder.encode(samsung.token(), StandardCharsets.UTF_8));
        }

        String separator = baseEndpoint.contains("?") ? "&" : "?";
        return URI.create(baseEndpoint + separator + query);
    }

    private String buildPayload(String remoteKey) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("method", "ms.remote.control");
        ObjectNode params = root.putObject("params");
        params.put("Cmd", "Click");
        params.put("DataOfCmd", remoteKey);
        params.put("Option", "false");
        params.put("TypeOfRemote", "SendRemoteKey");

        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize Samsung LAN payload.", exception);
        }
    }

    private static Map<RemoteCommand, String> remoteKeys() {
        Map<RemoteCommand, String> keys = new EnumMap<>(RemoteCommand.class);
        keys.put(RemoteCommand.POWER_TOGGLE, "KEY_POWER");
        keys.put(RemoteCommand.POWER_ON, "KEY_POWER");
        keys.put(RemoteCommand.POWER_OFF, "KEY_POWER");
        keys.put(RemoteCommand.VOLUME_UP, "KEY_VOLUP");
        keys.put(RemoteCommand.VOLUME_DOWN, "KEY_VOLDOWN");
        keys.put(RemoteCommand.MUTE, "KEY_MUTE");
        keys.put(RemoteCommand.CHANNEL_UP, "KEY_CHUP");
        keys.put(RemoteCommand.CHANNEL_DOWN, "KEY_CHDOWN");
        keys.put(RemoteCommand.HOME, "KEY_HOME");
        keys.put(RemoteCommand.BACK, "KEY_RETURN");
        keys.put(RemoteCommand.INPUT_SOURCE, "KEY_SOURCE");
        keys.put(RemoteCommand.DPAD_UP, "KEY_UP");
        keys.put(RemoteCommand.DPAD_DOWN, "KEY_DOWN");
        keys.put(RemoteCommand.DPAD_LEFT, "KEY_LEFT");
        keys.put(RemoteCommand.DPAD_RIGHT, "KEY_RIGHT");
        keys.put(RemoteCommand.OK, "KEY_ENTER");
        return keys;
    }
}
