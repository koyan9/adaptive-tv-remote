package io.github.koyan9.tvremote.persistence;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.PairingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PairingRepository extends JpaRepository<PairingEntity, String> {

    List<PairingEntity> findAllByTargetDevice_IdOrderByUpdatedAtDesc(String targetDeviceId);

    List<PairingEntity> findAllByTargetDevice_IdAndControlPathAndStatusOrderByUpdatedAtDesc(
            String targetDeviceId,
            ControlPath controlPath,
            PairingStatus status
    );

    long countByTargetDevice_Id(String targetDeviceId);

    long countByTargetDevice_IdAndControlPath(String targetDeviceId, ControlPath controlPath);
}
