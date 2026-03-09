package io.github.koyan9.tvremote.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<RoomEntity, String> {

    List<RoomEntity> findAllByOrderBySortOrderAsc();

    List<RoomEntity> findAllByHousehold_IdOrderBySortOrderAsc(String householdId);

    Optional<RoomEntity> findByHousehold_IdAndNameIgnoreCase(String householdId, String name);

    int countByHousehold_Id(String householdId);
}
