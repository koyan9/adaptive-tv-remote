package io.github.koyan9.tvremote.integration;

import io.github.koyan9.tvremote.brand.BrandDispatchPlan;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.ControlDecision;
import io.github.koyan9.tvremote.model.RemoteDevice;

public interface ProtocolClient {

    String clientKey();

    IntegrationMode integrationMode();

    String description();

    boolean supports(BrandDispatchPlan dispatchPlan);

    ProtocolDispatchResult dispatch(
            RemoteDevice device,
            RemoteCommand command,
            ControlDecision decision,
            BrandDispatchPlan dispatchPlan
    );
}


