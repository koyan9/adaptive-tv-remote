package io.github.koyan9.tvremote.integration;

public record ProtocolDispatchResult(
        String protocolClientKey,
        IntegrationMode integrationMode,
        String endpoint,
        String detail
) {
}


