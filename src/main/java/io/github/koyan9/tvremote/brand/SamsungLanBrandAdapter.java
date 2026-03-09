package io.github.koyan9.tvremote.brand;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.springframework.stereotype.Component;

@Component
public class SamsungLanBrandAdapter implements BrandCommandAdapter {

    @Override
    public String adapterKey() {
        return "samsung-lan";
    }

    @Override
    public String brand() {
        return "Samsung";
    }

    @Override
    public ControlPath path() {
        return ControlPath.LAN_DIRECT;
    }

    @Override
    public boolean supports(RemoteDevice device) {
        return "Samsung".equalsIgnoreCase(device.brand());
    }

    @Override
    public String protocolFamily() {
        return "Samsung Smart TV LAN Protocol (simulated)";
    }

    @Override
    public BrandDispatchPlan plan(RemoteDevice device, RemoteCommand command) {
        return new BrandDispatchPlan(
                adapterKey(),
                brand(),
                protocolFamily(),
                "Translated " + command + " into the Samsung LAN command frame for " + device.model() + "."
        );
    }
}


