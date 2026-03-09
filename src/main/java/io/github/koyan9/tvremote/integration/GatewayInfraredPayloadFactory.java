package io.github.koyan9.tvremote.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

@Component
public class GatewayInfraredPayloadFactory {

    private static final Map<RemoteCommand, String> ACTION_KEYS = actionKeys();

    private final ObjectMapper objectMapper;

    public GatewayInfraredPayloadFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public GatewayInfraredCommandRequest create(
            RemoteIntegrationProperties.Gateway gateway,
            RemoteDevice device,
            RemoteCommand command
    ) {
        String actionKey = ACTION_KEYS.get(command);
        if (actionKey == null) {
            throw new IllegalArgumentException("Infrared gateway does not support command " + command + " in the first real integration version.");
        }

        String profileKey = profileKey(device, actionKey);
        String payload = payload(gateway, device, command, profileKey);
        return new GatewayInfraredCommandRequest(
                URI.create(gateway.infraredEndpoint()),
                gateway.authToken(),
                gateway.hubId(),
                payload,
                profileKey
        );
    }

    private String payload(
            RemoteIntegrationProperties.Gateway gateway,
            RemoteDevice device,
            RemoteCommand command,
            String profileKey
    ) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("hubId", gateway.hubId());
        root.put("transport", "infrared");

        ObjectNode target = root.putObject("target");
        target.put("deviceId", device.id());
        target.put("brand", device.brand());
        target.put("model", device.model());
        target.put("room", device.room());

        ObjectNode commandNode = root.putObject("command");
        commandNode.put("name", command.name());
        commandNode.put("profileKey", profileKey);

        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize infrared gateway payload.", exception);
        }
    }

    private String profileKey(RemoteDevice device, String actionKey) {
        return (device.brand() + "-" + device.model() + "-" + actionKey)
                .toLowerCase(Locale.ROOT)
                .replace(' ', '-')
                .replaceAll("[^a-z0-9-]", "-")
                .replaceAll("-+", "-");
    }

    private static Map<RemoteCommand, String> actionKeys() {
        Map<RemoteCommand, String> keys = new EnumMap<>(RemoteCommand.class);
        keys.put(RemoteCommand.POWER_TOGGLE, "power-toggle");
        keys.put(RemoteCommand.POWER_ON, "power-on");
        keys.put(RemoteCommand.POWER_OFF, "power-off");
        keys.put(RemoteCommand.VOLUME_UP, "volume-up");
        keys.put(RemoteCommand.VOLUME_DOWN, "volume-down");
        keys.put(RemoteCommand.MUTE, "mute");
        keys.put(RemoteCommand.HOME, "home");
        keys.put(RemoteCommand.BACK, "back");
        keys.put(RemoteCommand.INPUT_SOURCE, "input-source");
        keys.put(RemoteCommand.DPAD_UP, "dpad-up");
        keys.put(RemoteCommand.DPAD_DOWN, "dpad-down");
        keys.put(RemoteCommand.DPAD_LEFT, "dpad-left");
        keys.put(RemoteCommand.DPAD_RIGHT, "dpad-right");
        keys.put(RemoteCommand.OK, "ok");
        return keys;
    }
}
