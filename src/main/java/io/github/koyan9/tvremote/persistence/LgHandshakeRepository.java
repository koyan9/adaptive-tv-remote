package io.github.koyan9.tvremote.persistence;

import io.github.koyan9.tvremote.domain.LgHandshakeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LgHandshakeRepository extends JpaRepository<LgHandshakeEntity, String> {

    List<LgHandshakeEntity> findAllByDevice_IdOrderByUpdatedAtDesc(String deviceId);

    Optional<LgHandshakeEntity> findFirstByDevice_IdAndStatusOrderByUpdatedAtDesc(String deviceId, LgHandshakeStatus status);
}
