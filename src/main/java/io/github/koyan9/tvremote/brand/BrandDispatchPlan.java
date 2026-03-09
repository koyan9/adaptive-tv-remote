package io.github.koyan9.tvremote.brand;

public record BrandDispatchPlan(
        String adapterKey,
        String brand,
        String protocolFamily,
        String dispatchMessage
) {
}


