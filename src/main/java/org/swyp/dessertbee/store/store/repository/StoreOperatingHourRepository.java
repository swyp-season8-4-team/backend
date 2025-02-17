package org.swyp.dessertbee.store.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.dessertbee.store.store.entity.StoreOperatingHour;

import java.util.List;

public interface StoreOperatingHourRepository extends JpaRepository<StoreOperatingHour, Long> {

    @Query("SELECT o FROM StoreOperatingHour o WHERE o.storeId = :storeId")
    List<StoreOperatingHour> findByStoreId(@Param("storeId") Long storeId);
}
