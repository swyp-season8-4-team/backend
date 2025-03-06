package org.swyp.dessertbee.community.review.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.swyp.dessertbee.community.review.entity.Review;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByReviewUuid(UUID communityReviewUuid);

    @Query("SELECT DISTINCT rc.name From ReviewCategory rc JOIN Review c ON rc.reviewCategoryId = c.reviewCategoryId WHERE rc.reviewCategoryId = :reviewCategoryId")
    String findNameByReviewCategoryId(Long reviewCategoryId);
}
