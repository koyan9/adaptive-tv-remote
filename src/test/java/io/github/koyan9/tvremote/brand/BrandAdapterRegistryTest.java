package io.github.koyan9.tvremote.brand;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.RemoteDevice;
import io.github.koyan9.tvremote.service.DeviceCatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BrandAdapterRegistryTest {

    @Autowired
    private DeviceCatalogService deviceCatalogService;

    @Autowired
    private BrandAdapterRegistry brandAdapterRegistry;

    @Test
    void prefersBrandSpecificAdapterWhenAvailable() {
        RemoteDevice device = deviceCatalogService.getDevice("tv-living-room");

        BrandDispatchPlan plan = brandAdapterRegistry.resolve(device, ControlPath.LAN_DIRECT, RemoteCommand.HOME);

        assertThat(plan.adapterKey()).isEqualTo("samsung-lan");
        assertThat(plan.protocolFamily()).contains("Samsung");
    }

    @Test
    void fallsBackToGenericInfraredAdapter() {
        RemoteDevice device = deviceCatalogService.getDevice("tv-guest-room");

        BrandDispatchPlan plan = brandAdapterRegistry.resolve(device, ControlPath.IR_GATEWAY, RemoteCommand.POWER_TOGGLE);

        assertThat(plan.adapterKey()).isEqualTo("generic-ir");
        assertThat(plan.protocolFamily()).contains("Infrared");
    }

    @Test
    void resolvesLgLanAdapterForLgTelevision() {
        RemoteDevice device = deviceCatalogService.getDevice("tv-studio");

        BrandDispatchPlan plan = brandAdapterRegistry.resolve(device, ControlPath.LAN_DIRECT, RemoteCommand.HOME);

        assertThat(plan.adapterKey()).isEqualTo("lg-lan");
        assertThat(plan.protocolFamily()).contains("LG webOS");
    }

    @Test
    void resolvesSonyLanAdapterForOnlineSonyTelevision() {
        RemoteDevice device = deviceCatalogService.getDevice("tv-office");

        BrandDispatchPlan plan = brandAdapterRegistry.resolve(device, ControlPath.LAN_DIRECT, RemoteCommand.HOME);

        assertThat(plan.adapterKey()).isEqualTo("sony-lan");
        assertThat(plan.protocolFamily()).contains("Sony BRAVIA");
    }
}


