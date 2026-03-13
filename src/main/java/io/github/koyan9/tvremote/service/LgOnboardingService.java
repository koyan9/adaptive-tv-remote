package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.LgHandshakeStatus;
import io.github.koyan9.tvremote.integration.LgLanHandshakeClient;
import io.github.koyan9.tvremote.integration.LgLanHandshakeRequest;
import io.github.koyan9.tvremote.integration.LgLanHandshakeRequestFactory;
import io.github.koyan9.tvremote.integration.LgLanHandshakeResult;
import io.github.koyan9.tvremote.integration.IntegrationConfigurationException;
import io.github.koyan9.tvremote.integration.IntegrationDisabledException;
import io.github.koyan9.tvremote.integration.IntegrationTimeoutException;
import io.github.koyan9.tvremote.integration.IntegrationTransportException;
import io.github.koyan9.tvremote.model.BrandOnboardingSessionSummary;
import io.github.koyan9.tvremote.persistence.CandidateDeviceEntity;
import io.github.koyan9.tvremote.persistence.DeviceEntity;
import io.github.koyan9.tvremote.persistence.DeviceRepository;
import io.github.koyan9.tvremote.persistence.LgHandshakeEntity;
import io.github.koyan9.tvremote.persistence.LgHandshakeRepository;
import io.github.koyan9.tvremote.config.RemoteIntegrationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LgOnboardingService implements BrandOnboardingProvider {

    private final DeviceRepository deviceRepository;
    private final LgHandshakeRepository lgHandshakeRepository;
    private final RemoteIntegrationProperties remoteIntegrationProperties;
    private final LgLanHandshakeRequestFactory lgLanHandshakeRequestFactory;
    private final LgLanHandshakeClient lgLanHandshakeClient;

    public LgOnboardingService(
            DeviceRepository deviceRepository,
            LgHandshakeRepository lgHandshakeRepository,
            RemoteIntegrationProperties remoteIntegrationProperties,
            LgLanHandshakeRequestFactory lgLanHandshakeRequestFactory,
            LgLanHandshakeClient lgLanHandshakeClient
    ) {
        this.deviceRepository = deviceRepository;
        this.lgHandshakeRepository = lgHandshakeRepository;
        this.remoteIntegrationProperties = remoteIntegrationProperties;
        this.lgLanHandshakeRequestFactory = lgLanHandshakeRequestFactory;
        this.lgLanHandshakeClient = lgLanHandshakeClient;
    }

    @Override
    public String providerId() {
        return "lg-lan-onboarding";
    }

    @Override
    public String brand() {
        return "LG";
    }

    @Override
    public boolean supportsCandidate(CandidateDeviceEntity candidate) {
        return "LG".equalsIgnoreCase(candidate.getBrand())
                && candidate.getAvailablePaths().contains(ControlPath.LAN_DIRECT);
    }

    @Override
    @Transactional
    public BrandOnboardingSessionSummary startOnboarding(String deviceId, String candidateId) {
        DeviceEntity device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Device not found: " + deviceId));

        LgLanHandshakeRequest request = lgLanHandshakeRequestFactory.create(remoteIntegrationProperties.lg(), candidateId, deviceId);
        try {
            LgLanHandshakeResult result = lgLanHandshakeClient.startHandshake(request);
            LgHandshakeEntity entity = lgHandshakeRepository.save(new LgHandshakeEntity(
                    UUID.randomUUID().toString(),
                    device,
                    candidateId,
                    result.endpoint().toString(),
                    request.clientIdentity(),
                    result.negotiatedClientKey(),
                    LgHandshakeStatus.COMPLETED,
                    result.detail()
            ));
            return toSummary(entity);
        } catch (RuntimeException exception) {
            if (!isOnboardingFailure(exception)) {
                throw exception;
            }
            LgHandshakeEntity entity = lgHandshakeRepository.save(new LgHandshakeEntity(
                    UUID.randomUUID().toString(),
                    device,
                    candidateId,
                    request.endpoint().toString(),
                    request.clientIdentity(),
                    null,
                    LgHandshakeStatus.FAILED,
                    exception.getMessage()
            ));
            return toSummary(entity);
        }
    }

    @Override
    public List<BrandOnboardingSessionSummary> sessions(String deviceId) {
        return lgHandshakeRepository.findAllByDevice_IdOrderByUpdatedAtDesc(deviceId).stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    public String latestNegotiatedCredential(String deviceId) {
        return lgHandshakeRepository.findFirstByDevice_IdAndStatusOrderByUpdatedAtDesc(deviceId, LgHandshakeStatus.COMPLETED)
                .map(LgHandshakeEntity::getNegotiatedClientKey)
                .filter(value -> value != null && !value.isBlank())
                .orElse(null);
    }

    private BrandOnboardingSessionSummary toSummary(LgHandshakeEntity entity) {
        return new BrandOnboardingSessionSummary(
                entity.getId(),
                providerId(),
                brand(),
                entity.getDevice().getId(),
                entity.getDevice().getDisplayName(),
                entity.getCandidateId(),
                entity.getEndpoint(),
                entity.getClientIdentity(),
                entity.getNegotiatedClientKey(),
                entity.getStatus().name(),
                entity.getDetail(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private boolean isOnboardingFailure(RuntimeException exception) {
        return exception instanceof IntegrationTransportException
                || exception instanceof IntegrationTimeoutException
                || exception instanceof IntegrationConfigurationException
                || exception instanceof IntegrationDisabledException;
    }
}
