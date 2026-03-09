package io.github.koyan9.tvremote.brand;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.model.RemoteDevice;
import io.github.koyan9.tvremote.service.DeviceCatalogService;
import io.github.koyan9.tvremote.service.ModelProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrandAdapterRegistryTest {

    private DeviceCatalogService deviceCatalogService;
    private BrandAdapterRegistry brandAdapterRegistry;

    @BeforeEach
    void setUp() {
        deviceCatalogService = new DeviceCatalogService(new ModelProfileService());
        brandAdapterRegistry = new BrandAdapterRegistry(List.of(
                new SamsungLanBrandAdapter(),
                new LgLanBrandAdapter(),
                new SonyLanBrandAdapter(),
                new GenericInfraredBrandAdapter(),
                new GenericHdmiCecBrandAdapter()
        ));
    }

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


