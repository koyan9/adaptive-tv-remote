package io.github.koyan9.tvremote.integration;

import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;

import java.net.URI;

final class SonyLanEndpoints {

    private SonyLanEndpoints() {
    }

    static URI resolveAccessControlEndpoint(RemoteIntegrationProperties.Sony sony) {
        URI configured = URI.create(sony.endpoint());
        if (hasSonyPath(configured)) {
            return configured;
        }
        return append(base(configured), "/sony/accessControl");
    }

    static URI resolveSystemEndpoint(RemoteIntegrationProperties.Sony sony) {
        URI configured = URI.create(sony.endpoint());
        if (pathEndsWith(configured, "/sony/system")) {
            return configured;
        }
        return append(base(configured), "/sony/system");
    }

    static URI resolveIrccEndpoint(RemoteIntegrationProperties.Sony sony) {
        if (sony.irccEndpoint() != null && !sony.irccEndpoint().isBlank()) {
            return URI.create(sony.irccEndpoint());
        }
        URI configured = URI.create(sony.endpoint());
        if (pathEndsWith(configured, "/sony/ircc")) {
            return configured;
        }
        return append(base(configured), "/sony/ircc");
    }

    private static boolean hasSonyPath(URI endpoint) {
        String path = endpoint.getPath();
        return path != null && path.contains("/sony/");
    }

    private static boolean pathEndsWith(URI endpoint, String suffix) {
        String path = endpoint.getPath();
        return path != null && path.toLowerCase().endsWith(suffix);
    }

    private static URI base(URI endpoint) {
        String scheme = endpoint.getScheme() == null ? "http" : endpoint.getScheme();
        String authority = endpoint.getAuthority();
        if (authority == null) {
            return URI.create(scheme + "://" + endpoint);
        }
        return URI.create(scheme + "://" + authority);
    }

    private static URI append(URI base, String path) {
        String baseUri = base.toString();
        if (baseUri.endsWith("/")) {
            baseUri = baseUri.substring(0, baseUri.length() - 1);
        }
        return URI.create(baseUri + path);
    }
}
