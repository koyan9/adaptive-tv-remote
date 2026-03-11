package io.github.koyan9.tvremote.integration;

public interface SonyLanSessionClient {

    SonyLanSessionResult sendCommand(SonyIrccCommandRequest request);
}
