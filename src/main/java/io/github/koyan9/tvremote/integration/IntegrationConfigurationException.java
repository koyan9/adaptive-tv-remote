package io.github.koyan9.tvremote.integration;

import java.util.List;

public class IntegrationConfigurationException extends RuntimeException {

    private final String adapterKey;
    private final IntegrationMode desiredMode;
    private final List<String> available;

    public IntegrationConfigurationException(String message) {
        this(message, null, null, List.of());
    }

    public IntegrationConfigurationException(String message, String adapterKey, IntegrationMode desiredMode, List<String> available) {
        super(message);
        this.adapterKey = adapterKey;
        this.desiredMode = desiredMode;
        this.available = available == null ? List.of() : List.copyOf(available);
    }

    public String getAdapterKey() {
        return adapterKey;
    }

    public IntegrationMode getDesiredMode() {
        return desiredMode;
    }

    public List<String> getAvailable() {
        return available;
    }
}
