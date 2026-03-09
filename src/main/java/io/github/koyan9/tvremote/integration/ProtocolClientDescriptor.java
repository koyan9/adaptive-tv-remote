package io.github.koyan9.tvremote.integration;

public record ProtocolClientDescriptor(
        String clientKey,
        IntegrationMode integrationMode,
        String description
) {
}


