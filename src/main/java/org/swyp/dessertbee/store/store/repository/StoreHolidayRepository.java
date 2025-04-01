package org.swyp.dessertbee.store.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.store.store.entity.StoreHoliday;

import java.util.List;

@Repository
public interface StoreHolidayRepository extends JpaRepository<StoreHoliday, Long> {

    @Query("SELECT h FROM StoreHoliday h WHERE h.storeId = :storeId")
    List<StoreHoliday> findByStoreId(@Param("storeId") Long storeId);

    @Transactional
    @Modifying
    @Query("DELETE FROM StoreHoliday h WHERE h.storeId = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);
}
