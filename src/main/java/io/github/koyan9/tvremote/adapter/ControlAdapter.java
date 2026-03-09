package io.github.koyan9.tvremote.adapter;

import io.github.koyan9.tvremote.brand.BrandDispatchPlan;
import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.integration.ProtocolDispatchResult;
import io.github.koyan9.tvremote.model.CommandResult;
import io.github.koyan9.tvremote.model.ControlDecision;
import io.github.koyan9.tvremote.model.RemoteDevice;

public interface ControlAdapter {

    ControlPath path();

    CommandResult execute(
            RemoteDevice device,
            RemoteCommand command,
            ControlDecision decision,
            BrandDispatchPlan dispatchPlan,
            ProtocolDispatchResult protocolDispatchResult
    );
}


