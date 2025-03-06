package org.swyp.dessertbee.community.review.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


    @Query("SELECT r FROM Review r " +
            "WHERE r.deletedAt IS NULL " +
            "AND (:reviewCategoryId IS NULL OR r.reviewCategoryId = : reviewCategoryId) " +
            "AND (:keyword IS NULL OR (r.title LIKE CONCAT('%', :keyword, '%') " +
            "     OR r.content LIKE CONCAT('%', :keyword, '%') " +
            "     OR r.placeName LIKE CONCAT('%', :keyword, '%'))) " +
            "ORDER BY r.reviewId DESC")
    Page<Review> findByDeletedAtIsNullAndReviewCategoryId(Pageable pageable, String keyword, Long reviewCategoryId);
}
