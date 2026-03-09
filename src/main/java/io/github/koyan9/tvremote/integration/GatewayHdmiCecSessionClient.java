package io.github.koyan9.tvremote.integration;

public interface GatewayHdmiCecSessionClient {

    GatewayHdmiCecSessionResult sendCommand(GatewayHdmiCecCommandRequest request);
}
