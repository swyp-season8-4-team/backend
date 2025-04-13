package org.swyp.dessertbee.statistics.store.repostiory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.statistics.store.entity.StoreReviewLog;
import org.swyp.dessertbee.statistics.store.entity.enums.ReviewAction;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoreReviewLogRepository extends JpaRepository<StoreReviewLog, Long> {
    // 특정 가게의 리뷰 로그 조회 (기간 지정)
    List<StoreReviewLog> findAllByStoreIdAndActionAtBetween(Long storeId, LocalDateTime start, LocalDateTime end);

    // 특정 가게의 리뷰 수 (CREATE만 카운팅)
    long countByStoreIdAndAction(Long storeId, ReviewAction action);
}