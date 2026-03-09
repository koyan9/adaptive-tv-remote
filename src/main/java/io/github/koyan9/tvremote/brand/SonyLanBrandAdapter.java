package io.github.koyan9.tvremote.brand;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.springframework.stereotype.Component;

@Component
public class SonyLanBrandAdapter implements BrandCommandAdapter {

    @Override
    public String adapterKey() {
        return "sony-lan";
    }

    @Override
    public String brand() {
        return "Sony";
    }

    @Override
    public ControlPath path() {
        return ControlPath.LAN_DIRECT;
    }

    @Override
    public boolean supports(RemoteDevice device) {
        return "Sony".equalsIgnoreCase(device.brand());
    }

    @Override
    public String protocolFamily() {
        return "Sony BRAVIA IP Control (simulated)";
    }

    @Override
    public BrandDispatchPlan plan(RemoteDevice device, RemoteCommand command) {
        return new BrandDispatchPlan(
                adapterKey(),
                brand(),
                protocolFamily(),
                "Translated " + command + " into the Sony BRAVIA IP payload for " + device.model() + "."
        );
    }
}


