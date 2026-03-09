package io.github.koyan9.tvremote.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "remote.project")
public record RemoteProjectProperties(
        String name,
        Boolean standalone,
        String currentMode,
        List<String> targetClients,
        String note
) {
    public RemoteProjectProperties {
        name = (name == null || name.isBlank()) ? "Adaptive TV Remote" : name;
        standalone = standalone == null ? Boolean.TRUE : standalone;
        currentMode = (currentMode == null || currentMode.isBlank()) ? "standalone-spring-boot-pwa" : currentMode;
        targetClients = targetClients == null ? List.of() : List.copyOf(targetClients);
        note = (note == null || note.isBlank())
                ? "Independent TV remote project with adaptive LAN, IR gateway, and HDMI-CEC routing."
                : note;
    }
}


