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
import java.util.Map;

@Component
public class GatewayHdmiCecPayloadFactory {

    private static final Map<RemoteCommand, String> ACTION_KEYS = actionKeys();

    private final ObjectMapper objectMapper;

    public GatewayHdmiCecPayloadFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public GatewayHdmiCecCommandRequest create(
            RemoteIntegrationProperties.Gateway gateway,
            RemoteDevice device,
            RemoteCommand command
    ) {
        String actionKey = ACTION_KEYS.get(command);
        if (actionKey == null) {
            throw new IllegalArgumentException("HDMI-CEC gateway does not support command " + command + " in the first real integration version.");
        }

        String payload = payload(gateway, device, command, actionKey);
        return new GatewayHdmiCecCommandRequest(
                URI.create(gateway.hdmiCecEndpoint()),
                gateway.authToken(),
                gateway.hubId(),
                payload,
                actionKey
        );
    }

    private String payload(
            RemoteIntegrationProperties.Gateway gateway,
            RemoteDevice device,
            RemoteCommand command,
            String actionKey
    ) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("hubId", gateway.hubId());
        root.put("transport", "hdmi-cec");

        ObjectNode target = root.putObject("target");
        target.put("deviceId", device.id());
        target.put("brand", device.brand());
        target.put("model", device.model());
        target.put("room", device.room());

        ObjectNode commandNode = root.putObject("command");
        commandNode.put("name", command.name());
        commandNode.put("actionKey", actionKey);

        String cecPayload = gateway.cecCommands().get(actionKey);
        if (cecPayload != null && !cecPayload.isBlank()) {
            commandNode.put("format", "cec-hex");
            ObjectNode cecNode = commandNode.putObject("cec");
            cecNode.put("payloadHex", cecPayload);
        } else {
            commandNode.put("format", "cec-action");
        }

        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize HDMI-CEC gateway payload.", exception);
        }
    }

    private static Map<RemoteCommand, String> actionKeys() {
        Map<RemoteCommand, String> keys = new EnumMap<>(RemoteCommand.class);
        keys.put(RemoteCommand.POWER_TOGGLE, "cec-power-toggle");
        keys.put(RemoteCommand.POWER_ON, "cec-power-on");
        keys.put(RemoteCommand.POWER_OFF, "cec-power-off");
        keys.put(RemoteCommand.VOLUME_UP, "cec-volume-up");
        keys.put(RemoteCommand.VOLUME_DOWN, "cec-volume-down");
        keys.put(RemoteCommand.MUTE, "cec-mute");
        keys.put(RemoteCommand.HOME, "cec-home");
        keys.put(RemoteCommand.BACK, "cec-back");
        keys.put(RemoteCommand.INPUT_SOURCE, "cec-input-source");
        keys.put(RemoteCommand.DPAD_UP, "cec-up");
        keys.put(RemoteCommand.DPAD_DOWN, "cec-down");
        keys.put(RemoteCommand.DPAD_LEFT, "cec-left");
        keys.put(RemoteCommand.DPAD_RIGHT, "cec-right");
        keys.put(RemoteCommand.OK, "cec-select");
        return keys;
    }
}
