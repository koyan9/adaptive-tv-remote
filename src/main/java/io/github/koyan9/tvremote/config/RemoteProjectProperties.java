package io.github.koyan9.tvremote.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "remote.project")
public record RemoteProjectProperties(
        String name,
        boolean standalone,
        String currentMode,
        List<String> targetClients,
        String note
) {
    public RemoteProjectProperties {
        targetClients = targetClients == null ? List.of() : List.copyOf(targetClients);
    }
}


