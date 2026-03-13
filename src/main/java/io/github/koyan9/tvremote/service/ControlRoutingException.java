package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.domain.ControlPath;

import java.util.List;

public class ControlRoutingException extends RuntimeException {

    private final ControlRoutingFailureReason reason;
    private final List<ControlPath> attemptedPaths;

    public ControlRoutingException(String message) {
        this(message, null, List.of());
    }

    public ControlRoutingException(String message, ControlRoutingFailureReason reason, List<ControlPath> attemptedPaths) {
        super(message);
        this.reason = reason;
        this.attemptedPaths = attemptedPaths == null ? List.of() : List.copyOf(attemptedPaths);
    }

    public ControlRoutingFailureReason getReason() {
        return reason;
    }

    public List<ControlPath> getAttemptedPaths() {
        return attemptedPaths;
    }
}


