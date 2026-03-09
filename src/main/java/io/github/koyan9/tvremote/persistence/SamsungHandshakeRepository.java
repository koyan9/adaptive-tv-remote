package io.github.koyan9.tvremote.persistence;

import io.github.koyan9.tvremote.domain.SamsungHandshakeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SamsungHandshakeRepository extends JpaRepository<SamsungHandshakeEntity, String> {

    List<SamsungHandshakeEntity> findAllByDevice_IdOrderByUpdatedAtDesc(String deviceId);

    Optional<SamsungHandshakeEntity> findFirstByDevice_IdAndStatusOrderByUpdatedAtDesc(String deviceId, SamsungHandshakeStatus status);
}
