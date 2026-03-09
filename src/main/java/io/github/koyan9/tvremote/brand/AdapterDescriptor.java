package io.github.koyan9.tvremote.brand;

import io.github.koyan9.tvremote.domain.ControlPath;

public record AdapterDescriptor(
        String adapterKey,
        String brand,
        ControlPath path,
        String protocolFamily
) {
}


