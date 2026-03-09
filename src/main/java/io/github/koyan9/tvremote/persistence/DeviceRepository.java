package io.github.koyan9.tvremote.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<DeviceEntity, String> {

    List<DeviceEntity> findAllByOrderBySortOrderAsc();

    int countByRoom_Household_Id(String householdId);

    int countByRoom_Id(String roomId);
}
