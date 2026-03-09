package io.github.koyan9.tvremote.brand;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.RemoteDevice;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class BrandAdapterRegistry {

    private final List<BrandCommandAdapter> adapters;

    public BrandAdapterRegistry(List<BrandCommandAdapter> adapters) {
        this.adapters = List.copyOf(adapters);
    }

    public BrandDispatchPlan resolve(RemoteDevice device, ControlPath path, RemoteCommand command) {
        return adapters.stream()
                .filter(adapter -> adapter.path() == path)
                .filter(adapter -> adapter.supports(device))
                .sorted(Comparator.comparing(adapter -> isGeneric(adapter.brand()) ? 1 : 0))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No brand adapter registered for " + device.brand() + " on path " + path))
                .plan(device, command);
    }

    public List<AdapterDescriptor> descriptors() {
        return adapters.stream()
                .map(adapter -> new AdapterDescriptor(adapter.adapterKey(), adapter.brand(), adapter.path(), adapter.protocolFamily()))
                .toList();
    }

    private boolean isGeneric(String brand) {
        return "generic".equals(brand.toLowerCase(Locale.ROOT));
    }
}


