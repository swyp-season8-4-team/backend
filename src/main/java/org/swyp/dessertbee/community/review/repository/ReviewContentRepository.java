package org.swyp.dessertbee.community.review.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.community.review.entity.ReviewContent;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewContentRepository extends JpaRepository<ReviewContent, Long> {
    List<ReviewContent> findByReviewIdOrderByReviewIdAsc(Long reviewId);

    Optional<ReviewContent> findByReviewIdAndDeletedAtIsNull(Long reviewId);

    List<ReviewContent> findByReviewIdAndType(Long reviewId, String image);

    void deleteByReviewId(Long reviewId);

}
