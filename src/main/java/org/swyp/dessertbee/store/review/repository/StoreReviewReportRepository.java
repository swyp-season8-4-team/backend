package org.swyp.dessertbee.store.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.community.mate.entity.MateReport;
import org.swyp.dessertbee.store.review.entity.StoreReviewReport;

@Repository
public interface StoreReviewReportRepository extends JpaRepository<StoreReviewReport, Long> {
    StoreReviewReport findByReviewIdAndUserId(Long reviewId, Long id);
}
