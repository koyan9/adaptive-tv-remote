package io.github.koyan9.tvremote.brand;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.springframework.stereotype.Component;

@Component
public class GenericHdmiCecBrandAdapter implements BrandCommandAdapter {

    @Override
    public String adapterKey() {
        return "generic-hdmi-cec";
    }

    @Override
    public String brand() {
        return "Generic";
    }

    @Override
    public ControlPath path() {
        return ControlPath.HDMI_CEC_GATEWAY;
    }

    @Override
    public boolean supports(RemoteDevice device) {
        return true;
    }

    @Override
    public String protocolFamily() {
        return "HDMI-CEC Bridge (simulated)";
    }

    @Override
    public BrandDispatchPlan plan(RemoteDevice device, RemoteCommand command) {
        return new BrandDispatchPlan(
                adapterKey(),
                device.brand(),
                protocolFamily(),
                "Wrapped " + command + " into an HDMI-CEC action for downstream equipment control."
        );
    }
}


