package io.github.koyan9.tvremote.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HouseholdRepository extends JpaRepository<HouseholdEntity, String> {

    Optional<HouseholdEntity> findFirstByOrderBySortOrderAsc();
}
