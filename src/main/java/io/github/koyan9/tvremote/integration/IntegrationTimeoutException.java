package io.github.koyan9.tvremote.integration;

public class IntegrationTimeoutException extends IntegrationTransportException {

    public IntegrationTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
