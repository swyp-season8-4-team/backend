package org.swyp.dessertbee.store.store.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.store.entity.StoreOperatingHour;

import java.util.List;

@Repository
public interface StoreOperatingHourRepository extends JpaRepository<StoreOperatingHour, Long> {

    @Query("SELECT o FROM StoreOperatingHour o WHERE o.storeId = :storeId")
    List<StoreOperatingHour> findByStoreId(@Param("storeId") Long storeId);

    @Transactional
    @Modifying
    @Query("DELETE FROM StoreOperatingHour o WHERE o.storeId = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);
}
