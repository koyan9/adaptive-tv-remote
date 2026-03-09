package io.github.koyan9.tvremote.brand;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.springframework.stereotype.Component;

@Component
public class LgLanBrandAdapter implements BrandCommandAdapter {

    @Override
    public String adapterKey() {
        return "lg-lan";
    }

    @Override
    public String brand() {
        return "LG";
    }

    @Override
    public ControlPath path() {
        return ControlPath.LAN_DIRECT;
    }

    @Override
    public boolean supports(RemoteDevice device) {
        return "LG".equalsIgnoreCase(device.brand());
    }

    @Override
    public String protocolFamily() {
        return "LG webOS TV SSAP (simulated)";
    }

    @Override
    public BrandDispatchPlan plan(RemoteDevice device, RemoteCommand command) {
        return new BrandDispatchPlan(
                adapterKey(),
                brand(),
                protocolFamily(),
                "Translated " + command + " into the LG webOS SSAP payload for " + device.model() + "."
        );
    }
}


