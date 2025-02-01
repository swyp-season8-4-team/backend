package org.swyp.dessertbee.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.store.entity.StoreStatistics;

import java.util.Optional;

@Repository
public interface StoreStatisticsRepository extends JpaRepository<StoreStatistics, Long> {

    // 특정 가게의 통계 조회
    Optional<StoreStatistics> findByStoreId(Long storeId);
}
