package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.domain.SamsungHandshakeStatus;
import io.github.koyan9.tvremote.integration.SamsungLanHandshakeClient;
import io.github.koyan9.tvremote.integration.SamsungLanHandshakeRequest;
import io.github.koyan9.tvremote.integration.SamsungLanHandshakeRequestFactory;
import io.github.koyan9.tvremote.integration.SamsungLanHandshakeResult;
import io.github.koyan9.tvremote.model.BrandOnboardingSessionSummary;
import io.github.koyan9.tvremote.model.SamsungHandshakeSummary;
import io.github.koyan9.tvremote.persistence.CandidateDeviceEntity;
import io.github.koyan9.tvremote.persistence.DeviceEntity;
import io.github.koyan9.tvremote.persistence.DeviceRepository;
import io.github.koyan9.tvremote.persistence.SamsungHandshakeEntity;
import io.github.koyan9.tvremote.persistence.SamsungHandshakeRepository;
import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SamsungOnboardingService implements BrandOnboardingProvider {

    private final DeviceRepository deviceRepository;
    private final SamsungHandshakeRepository samsungHandshakeRepository;
    private final RemoteIntegrationProperties remoteIntegrationProperties;
    private final SamsungLanHandshakeRequestFactory samsungLanHandshakeRequestFactory;
    private final SamsungLanHandshakeClient samsungLanHandshakeClient;

    public SamsungOnboardingService(
            DeviceRepository deviceRepository,
            SamsungHandshakeRepository samsungHandshakeRepository,
            RemoteIntegrationProperties remoteIntegrationProperties,
            SamsungLanHandshakeRequestFactory samsungLanHandshakeRequestFactory,
            SamsungLanHandshakeClient samsungLanHandshakeClient
    ) {
        this.deviceRepository = deviceRepository;
        this.samsungHandshakeRepository = samsungHandshakeRepository;
        this.remoteIntegrationProperties = remoteIntegrationProperties;
        this.samsungLanHandshakeRequestFactory = samsungLanHandshakeRequestFactory;
        this.samsungLanHandshakeClient = samsungLanHandshakeClient;
    }

    @Transactional
    public SamsungHandshakeSummary startHandshake(String deviceId, String candidateId) {
        DeviceEntity device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Device not found: " + deviceId));

        SamsungLanHandshakeRequest request = samsungLanHandshakeRequestFactory.create(remoteIntegrationProperties.samsung(), candidateId, deviceId);
        SamsungLanHandshakeResult result = samsungLanHandshakeClient.startHandshake(request);

        SamsungHandshakeEntity entity = samsungHandshakeRepository.save(new SamsungHandshakeEntity(
                UUID.randomUUID().toString(),
                device,
                candidateId,
                result.endpoint().toString(),
                request.clientName(),
                result.negotiatedToken(),
                SamsungHandshakeStatus.COMPLETED,
                result.detail()
        ));
        return toSummary(entity);
    }

    @Override
    public String providerId() {
        return "samsung-lan-onboarding";
    }

    @Override
    public String brand() {
        return "Samsung";
    }

    @Override
    public boolean supportsCandidate(CandidateDeviceEntity candidate) {
        return "Samsung".equalsIgnoreCase(candidate.getBrand())
                && candidate.getAvailablePaths().contains(io.github.koyan9.tvremote.domain.ControlPath.LAN_DIRECT);
    }

    @Override
    public BrandOnboardingSessionSummary startOnboarding(String deviceId, String candidateId) {
        return toGenericSummary(startHandshake(deviceId, candidateId));
    }

    @Override
    public List<BrandOnboardingSessionSummary> sessions(String deviceId) {
        return handshakes(deviceId).stream().map(this::toGenericSummary).toList();
    }

    @Override
    public String latestNegotiatedCredential(String deviceId) {
        return latestNegotiatedToken(deviceId);
    }

    public List<SamsungHandshakeSummary> handshakes(String deviceId) {
        return samsungHandshakeRepository.findAllByDevice_IdOrderByUpdatedAtDesc(deviceId).stream()
                .map(this::toSummary)
                .toList();
    }

    public String latestNegotiatedToken(String deviceId) {
        return samsungHandshakeRepository.findFirstByDevice_IdAndStatusOrderByUpdatedAtDesc(deviceId, SamsungHandshakeStatus.COMPLETED)
                .map(SamsungHandshakeEntity::getNegotiatedToken)
                .filter(token -> token != null && !token.isBlank())
                .orElse(null);
    }

    private SamsungHandshakeSummary toSummary(SamsungHandshakeEntity entity) {
        return new SamsungHandshakeSummary(
                entity.getId(),
                entity.getDevice().getId(),
                entity.getDevice().getDisplayName(),
                entity.getCandidateId(),
                entity.getEndpoint(),
                entity.getClientName(),
                entity.getNegotiatedToken(),
                entity.getStatus(),
                entity.getDetail(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private BrandOnboardingSessionSummary toGenericSummary(SamsungHandshakeSummary summary) {
        return new BrandOnboardingSessionSummary(
                summary.id(),
                providerId(),
                brand(),
                summary.deviceId(),
                summary.deviceName(),
                summary.candidateId(),
                summary.endpoint(),
                summary.clientName(),
                summary.negotiatedToken(),
                summary.status().name(),
                summary.detail(),
                summary.createdAt(),
                summary.updatedAt()
        );
    }
}
