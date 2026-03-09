package io.github.koyan9.tvremote.brand;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.RemoteDevice;

public interface BrandCommandAdapter {

    String adapterKey();

    String brand();

    ControlPath path();

    boolean supports(RemoteDevice device);

    String protocolFamily();

    BrandDispatchPlan plan(RemoteDevice device, RemoteCommand command);
}


