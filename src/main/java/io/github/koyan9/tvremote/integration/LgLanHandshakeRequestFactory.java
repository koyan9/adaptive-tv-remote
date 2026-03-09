package io.github.koyan9.tvremote.integration;

import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class LgLanHandshakeRequestFactory {

    public LgLanHandshakeRequest create(RemoteIntegrationProperties.Lg lg, String candidateId, String deviceId) {
        return new LgLanHandshakeRequest(URI.create(lg.endpoint()), "AdaptiveTvRemote-LG", candidateId, deviceId);
    }
}
