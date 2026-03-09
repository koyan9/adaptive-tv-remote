package io.github.koyan9.tvremote.model;

import io.github.koyan9.tvremote.domain.RemoteCommand;
import jakarta.validation.constraints.NotNull;

public record CommandRequest(
        @NotNull RemoteCommand command,
        String preferredGatewayId
) {
}


