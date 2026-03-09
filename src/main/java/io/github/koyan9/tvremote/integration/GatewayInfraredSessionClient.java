package io.github.koyan9.tvremote.integration;

public interface GatewayInfraredSessionClient {

    GatewayInfraredSessionResult sendCommand(GatewayInfraredCommandRequest request);
}
