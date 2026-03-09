package io.github.koyan9.tvremote.integration;

import io.github.koyan9.tvremote.brand.BrandDispatchPlan;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.ControlDecision;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.springframework.stereotype.Component;

@Component
public class MockProtocolClient implements ProtocolClient {

    @Override
    public String clientKey() {
        return "mock-protocol-client";
    }

    @Override
    public IntegrationMode integrationMode() {
        return IntegrationMode.MOCK;
    }

    @Override
    public String description() {
        return "Default simulated protocol client for all adapters.";
    }

    @Override
    public boolean supports(BrandDispatchPlan dispatchPlan) {
        return true;
    }

    @Override
    public ProtocolDispatchResult dispatch(RemoteDevice device, RemoteCommand command, ControlDecision decision, BrandDispatchPlan dispatchPlan) {
        return new ProtocolDispatchResult(
                clientKey(),
                integrationMode(),
                "simulated://" + dispatchPlan.adapterKey(),
                "Simulated protocol dispatch for " + device.brand() + " " + device.model() + " using " + command + "."
        );
    }
}


