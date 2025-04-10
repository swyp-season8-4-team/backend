package org.swyp.dessertbee.statistics.store.repostiory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.dessertbee.statistics.store.entity.CommunityReviewLog;
import org.swyp.dessertbee.statistics.store.entity.enums.ReviewAction;

import java.time.LocalDateTime;
import java.util.List;

public interface CommunityReviewLogRepository extends JpaRepository<CommunityReviewLog, Long> {

    // 특정 가게의 커뮤니티 리뷰 로그 조회 (기간 지정)
    List<CommunityReviewLog> findAllByStoreIdAndActionAtBetween(Long storeId, LocalDateTime start, LocalDateTime end);

    // CREATE 수만 카운팅
    long countByStoreIdAndAction(Long storeId, ReviewAction action);
}
