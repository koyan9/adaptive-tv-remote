package io.github.koyan9.tvremote.integration;

import io.github.koyan9.tvremote.domain.RemoteCommand;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class SonyIrccPayloadFactory {

    private static final Map<RemoteCommand, String> IRCC_CODES = irccCodes();

    public SonyIrccCommandRequest create(String endpoint, String preSharedKey, RemoteCommand command) {
        String irccCode = IRCC_CODES.get(command);
        if (irccCode == null) {
            throw new IllegalArgumentException("Sony IRCC does not support command " + command + " in the first real integration version.");
        }
        return new SonyIrccCommandRequest(
                endpoint,
                preSharedKey,
                command,
                irccCode,
                payload(irccCode)
        );
    }

    private String payload(String irccCode) {
        return """
                <?xml version="1.0" encoding="utf-8"?>
                <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
                  <s:Body>
                    <u:X_SendIRCC xmlns:u="urn:schemas-sony-com:service:IRCC:1">
                      <IRCCCode>%s</IRCCCode>
                    </u:X_SendIRCC>
                  </s:Body>
                </s:Envelope>
                """.formatted(irccCode);
    }

    private static Map<RemoteCommand, String> irccCodes() {
        Map<RemoteCommand, String> codes = new EnumMap<>(RemoteCommand.class);
        codes.put(RemoteCommand.POWER_TOGGLE, "AAAAAQAAAAEAAAAVAw=="); // Power
        codes.put(RemoteCommand.POWER_ON, "AAAAAQAAAAEAAAAVAw==");
        codes.put(RemoteCommand.POWER_OFF, "AAAAAQAAAAEAAAAVAw==");
        codes.put(RemoteCommand.INPUT_SOURCE, "AAAAAQAAAAEAAAAlAw=="); // Input
        codes.put(RemoteCommand.HOME, "AAAAAQAAAAEAAABgAw=="); // Home
        codes.put(RemoteCommand.BACK, "AAAAAgAAAJcAAAAjAw=="); // Return
        codes.put(RemoteCommand.DPAD_UP, "AAAAAQAAAAEAAAB0Aw=="); // Up
        codes.put(RemoteCommand.DPAD_DOWN, "AAAAAQAAAAEAAAB1Aw=="); // Down
        codes.put(RemoteCommand.DPAD_LEFT, "AAAAAQAAAAEAAAA0Aw=="); // Left
        codes.put(RemoteCommand.DPAD_RIGHT, "AAAAAQAAAAEAAAAzAw=="); // Right
        codes.put(RemoteCommand.OK, "AAAAAQAAAAEAAABlAw=="); // Confirm
        codes.put(RemoteCommand.VOLUME_UP, "AAAAAQAAAAEAAAASAw==");
        codes.put(RemoteCommand.VOLUME_DOWN, "AAAAAQAAAAEAAAATAw==");
        codes.put(RemoteCommand.MUTE, "AAAAAQAAAAEAAAAUAw==");
        codes.put(RemoteCommand.CHANNEL_UP, "AAAAAQAAAAEAAAAQAw==");
        codes.put(RemoteCommand.CHANNEL_DOWN, "AAAAAQAAAAEAAAARAw==");
        return codes;
    }
}
