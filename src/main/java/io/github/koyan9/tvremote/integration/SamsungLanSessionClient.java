package io.github.koyan9.tvremote.integration;

public interface SamsungLanSessionClient {

    SamsungLanSessionResult sendCommand(SamsungLanCommandRequest request);
}
