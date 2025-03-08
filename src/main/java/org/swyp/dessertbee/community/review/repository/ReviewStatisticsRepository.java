package org.swyp.dessertbee.community.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.community.review.entity.ReviewStatistics;

@Repository
public interface ReviewStatisticsRepository extends JpaRepository<ReviewStatistics, Long> {
    ReviewStatistics findByReviewId(Long reviewId);
}
