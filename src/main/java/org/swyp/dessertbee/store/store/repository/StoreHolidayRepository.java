package org.swyp.dessertbee.store.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.dessertbee.store.store.entity.StoreHoliday;

import java.util.List;

public interface StoreHolidayRepository extends JpaRepository<StoreHoliday, Long> {

    @Query("SELECT h FROM StoreHoliday h WHERE h.storeId = :storeId")
    List<StoreHoliday> findByStoreId(@Param("storeId") Long storeId);
}
