package io.github.koyan9.tvremote.integration;

public class IntegrationTransportException extends RuntimeException {

    public IntegrationTransportException(String message) {
        super(message);
    }

    public IntegrationTransportException(String message, Throwable cause) {
        super(message, cause);
    }
}
