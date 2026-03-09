package io.github.koyan9.tvremote.persistence;

import io.github.koyan9.tvremote.domain.SonyHandshakeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SonyHandshakeRepository extends JpaRepository<SonyHandshakeEntity, String> {

    List<SonyHandshakeEntity> findAllByDevice_IdOrderByUpdatedAtDesc(String deviceId);

    Optional<SonyHandshakeEntity> findFirstByDevice_IdAndStatusOrderByUpdatedAtDesc(String deviceId, SonyHandshakeStatus status);
}
