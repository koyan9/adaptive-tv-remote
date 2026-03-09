package io.github.koyan9.tvremote.adapter;

import io.github.koyan9.tvremote.brand.BrandDispatchPlan;
import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.integration.ProtocolDispatchResult;
import io.github.koyan9.tvremote.model.CommandResult;
import io.github.koyan9.tvremote.model.ControlDecision;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class InfraredGatewayControlAdapter implements ControlAdapter {

    @Override
    public ControlPath path() {
        return ControlPath.IR_GATEWAY;
    }

    @Override
    public CommandResult execute(
            RemoteDevice device,
            RemoteCommand command,
            ControlDecision decision,
            BrandDispatchPlan dispatchPlan,
            ProtocolDispatchResult protocolDispatchResult
    ) {
        return new CommandResult(
                UUID.randomUUID().toString(),
                device.id(),
                device.displayName(),
                command,
                path(),
                decision.gatewayDeviceId(),
                decision.adapterLabel(),
                dispatchPlan.adapterKey(),
                dispatchPlan.protocolFamily(),
                protocolDispatchResult.protocolClientKey(),
                protocolDispatchResult.integrationMode().name(),
                protocolDispatchResult.endpoint(),
                "SUCCESS",
                protocolDispatchResult.detail(),
                Instant.now()
        );
    }
}


