package io.github.koyan9.tvremote.config;

import io.github.koyan9.tvremote.integration.IntegrationMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "remote.integration")
public record RemoteIntegrationProperties(
        String defaultMode,
        Map<String, String> adapterModes,
        Samsung samsung,
        Sony sony,
        Lg lg,
        Gateway gateway
) {
    public RemoteIntegrationProperties {
        defaultMode = defaultMode == null || defaultMode.isBlank() ? "mock" : defaultMode;
        adapterModes = adapterModes == null ? Map.of() : Map.copyOf(adapterModes);
        samsung = samsung == null ? new Samsung(true, "ws://192.168.50.21:8001/api/v2/channels/samsung.remote.control", "demo-samsung-token") : samsung;
        sony = sony == null ? new Sony(true, "http://192.168.50.41/sony/accessControl", "demo-psk") : sony;
        lg = lg == null ? new Lg(true, "ws://192.168.50.31:3000", "demo-lg-client-key") : lg;
        gateway = gateway == null ? new Gateway(
                true,
                "http://gateway.local",
                "http://gateway.local/api/infrared/send",
                "http://gateway.local/api/cec/send",
                "hub-one-demo",
                "demo-gateway-token"
        ) : gateway;
    }

    public IntegrationMode modeFor(String adapterKey) {
        return IntegrationMode.from(adapterModes.getOrDefault(adapterKey, defaultMode));
    }

    public record Samsung(
            boolean enabled,
            String endpoint,
            String token
    ) {
    }

    public record Sony(
            boolean enabled,
            String endpoint,
            String preSharedKey
    ) {
    }

    public record Lg(
            boolean enabled,
            String endpoint,
            String clientKey
    ) {
    }

    public record Gateway(
            boolean enabled,
            String endpoint,
            String infraredEndpoint,
            String hdmiCecEndpoint,
            String hubId,
            String authToken
    ) {
    }
}


