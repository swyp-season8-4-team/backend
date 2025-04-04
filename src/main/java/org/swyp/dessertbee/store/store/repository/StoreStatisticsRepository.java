package org.swyp.dessertbee.store.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.store.entity.StoreStatistics;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreStatisticsRepository extends JpaRepository<StoreStatistics, Long> {

    // 특정 가게의 통계 조회
    Optional<StoreStatistics> findByStoreId(Long storeId);
}
