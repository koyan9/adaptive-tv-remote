package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.SonyHandshakeStatus;
import io.github.koyan9.tvremote.integration.SonyLanHandshakeClient;
import io.github.koyan9.tvremote.integration.SonyLanHandshakeRequest;
import io.github.koyan9.tvremote.integration.SonyLanHandshakeRequestFactory;
import io.github.koyan9.tvremote.integration.SonyLanHandshakeResult;
import io.github.koyan9.tvremote.model.BrandOnboardingSessionSummary;
import io.github.koyan9.tvremote.persistence.CandidateDeviceEntity;
import io.github.koyan9.tvremote.persistence.DeviceEntity;
import io.github.koyan9.tvremote.persistence.DeviceRepository;
import io.github.koyan9.tvremote.persistence.SonyHandshakeEntity;
import io.github.koyan9.tvremote.persistence.SonyHandshakeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SonyOnboardingService implements BrandOnboardingProvider {

    private final DeviceRepository deviceRepository;
    private final SonyHandshakeRepository sonyHandshakeRepository;
    private final RemoteIntegrationProperties remoteIntegrationProperties;
    private final SonyLanHandshakeRequestFactory sonyLanHandshakeRequestFactory;
    private final SonyLanHandshakeClient sonyLanHandshakeClient;

    public SonyOnboardingService(
            DeviceRepository deviceRepository,
            SonyHandshakeRepository sonyHandshakeRepository,
            RemoteIntegrationProperties remoteIntegrationProperties,
            SonyLanHandshakeRequestFactory sonyLanHandshakeRequestFactory,
            SonyLanHandshakeClient sonyLanHandshakeClient
    ) {
        this.deviceRepository = deviceRepository;
        this.sonyHandshakeRepository = sonyHandshakeRepository;
        this.remoteIntegrationProperties = remoteIntegrationProperties;
        this.sonyLanHandshakeRequestFactory = sonyLanHandshakeRequestFactory;
        this.sonyLanHandshakeClient = sonyLanHandshakeClient;
    }

    @Override
    public String providerId() {
        return "sony-lan-onboarding";
    }

    @Override
    public String brand() {
        return "Sony";
    }

    @Override
    public boolean supportsCandidate(CandidateDeviceEntity candidate) {
        return "Sony".equalsIgnoreCase(candidate.getBrand())
                && candidate.getAvailablePaths().contains(ControlPath.LAN_DIRECT);
    }

    @Override
    @Transactional
    public BrandOnboardingSessionSummary startOnboarding(String deviceId, String candidateId) {
        DeviceEntity device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Device not found: " + deviceId));

        SonyLanHandshakeRequest request = sonyLanHandshakeRequestFactory.create(remoteIntegrationProperties.sony(), candidateId, deviceId);
        SonyLanHandshakeResult result = sonyLanHandshakeClient.startHandshake(request);

        SonyHandshakeEntity entity = sonyHandshakeRepository.save(new SonyHandshakeEntity(
                UUID.randomUUID().toString(),
                device,
                candidateId,
                result.endpoint().toString(),
                request.clientIdentity(),
                result.negotiatedPreSharedKey(),
                SonyHandshakeStatus.COMPLETED,
                result.detail()
        ));
        return toSummary(entity);
    }

    @Override
    public List<BrandOnboardingSessionSummary> sessions(String deviceId) {
        return sonyHandshakeRepository.findAllByDevice_IdOrderByUpdatedAtDesc(deviceId).stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    public String latestNegotiatedCredential(String deviceId) {
        return sonyHandshakeRepository.findFirstByDevice_IdAndStatusOrderByUpdatedAtDesc(deviceId, SonyHandshakeStatus.COMPLETED)
                .map(SonyHandshakeEntity::getNegotiatedPreSharedKey)
                .filter(value -> value != null && !value.isBlank())
                .orElse(null);
    }

    private BrandOnboardingSessionSummary toSummary(SonyHandshakeEntity entity) {
        return new BrandOnboardingSessionSummary(
                entity.getId(),
                providerId(),
                brand(),
                entity.getDevice().getId(),
                entity.getDevice().getDisplayName(),
                entity.getCandidateId(),
                entity.getEndpoint(),
                entity.getClientIdentity(),
                entity.getNegotiatedPreSharedKey(),
                entity.getStatus().name(),
                entity.getDetail(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
