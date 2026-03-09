package io.github.koyan9.tvremote.brand;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.springframework.stereotype.Component;

@Component
public class GenericInfraredBrandAdapter implements BrandCommandAdapter {

    @Override
    public String adapterKey() {
        return "generic-ir";
    }

    @Override
    public String brand() {
        return "Generic";
    }

    @Override
    public ControlPath path() {
        return ControlPath.IR_GATEWAY;
    }

    @Override
    public boolean supports(RemoteDevice device) {
        return true;
    }

    @Override
    public String protocolFamily() {
        return "Infrared Code Library (simulated)";
    }

    @Override
    public BrandDispatchPlan plan(RemoteDevice device, RemoteCommand command) {
        return new BrandDispatchPlan(
                adapterKey(),
                device.brand(),
                protocolFamily(),
                "Mapped " + command + " to a learned IR code profile for " + device.brand() + " " + device.model() + "."
        );
    }
}


