package org.swyp.dessertbee.community.review.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.community.review.entity.Review;
import org.swyp.dessertbee.community.review.entity.SavedReview;

@Repository
public interface SavedReviewRepository extends JpaRepository<SavedReview, Long> {
    SavedReview findBySavedReviewIdAndUserId(Long reviewId, Long userId);

    SavedReview findByReview_ReviewIdAndUserId(Long reviewId, Long userId);

    Page<SavedReview> findByUserId(Pageable pageable, Long currentUserId);
}
