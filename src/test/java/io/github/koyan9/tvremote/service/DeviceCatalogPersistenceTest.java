package io.github.koyan9.tvremote.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DeviceCatalogPersistenceTest {

    @Autowired
    private DeviceCatalogService deviceCatalogService;

    @Test
    void loadsSeededDevicesFromDatabaseAndExposesHouseholdNetworkName() {
        assertThat(deviceCatalogService.primaryHouseholdNetworkName()).isEqualTo("Koyan Home Mesh");
        assertThat(deviceCatalogService.allDevices())
                .isNotEmpty()
                .first()
                .extracting(device -> device.id())
                .isEqualTo("tv-living-room");
    }
}
