package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.model.DiscoveryResult;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DiscoveryService {

    private final DeviceCatalogService deviceCatalogService;

    public DiscoveryService(DeviceCatalogService deviceCatalogService) {
        this.deviceCatalogService = deviceCatalogService;
    }

    public DiscoveryResult scanHomeNetwork() {
        return new DiscoveryResult(
                deviceCatalogService.primaryHouseholdNetworkName(),
                Instant.now(),
                deviceCatalogService.allDevices()
        );
    }
}


