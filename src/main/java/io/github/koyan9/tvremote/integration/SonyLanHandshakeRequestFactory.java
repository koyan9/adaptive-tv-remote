package io.github.koyan9.tvremote.integration;

import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class SonyLanHandshakeRequestFactory {

    public SonyLanHandshakeRequest create(RemoteIntegrationProperties.Sony sony, String candidateId, String deviceId) {
        return new SonyLanHandshakeRequest(
                SonyLanEndpoints.resolveSystemEndpoint(sony),
                "AdaptiveTvRemote-Sony",
                candidateId,
                deviceId,
                sony.preSharedKey()
        );
    }
}
