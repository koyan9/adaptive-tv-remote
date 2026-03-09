package io.github.koyan9.tvremote.persistence;

import io.github.koyan9.tvremote.domain.CandidateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateDeviceRepository extends JpaRepository<CandidateDeviceEntity, String> {

    List<CandidateDeviceEntity> findAllByStatusOrderBySortOrderAsc(CandidateStatus status);

    List<CandidateDeviceEntity> findAllByOrderBySortOrderAsc();

    long countByStatus(CandidateStatus status);
}
