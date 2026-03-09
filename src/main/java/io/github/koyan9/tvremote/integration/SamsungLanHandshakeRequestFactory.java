package io.github.koyan9.tvremote.integration;

import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class SamsungLanHandshakeRequestFactory {

    public SamsungLanHandshakeRequest create(RemoteIntegrationProperties.Samsung samsung, String candidateId, String deviceId) {
        String encodedName = Base64.getEncoder().encodeToString(samsung.clientName().getBytes(StandardCharsets.UTF_8));
        String separator = samsung.endpoint().contains("?") ? "&" : "?";
        URI endpoint = URI.create(samsung.endpoint() + separator + "name=" + URLEncoder.encode(encodedName, StandardCharsets.UTF_8));
        return new SamsungLanHandshakeRequest(endpoint, samsung.clientName(), candidateId, deviceId);
    }
}
