package org.swyp.dessertbee.community.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.dessertbee.community.review.entity.ReviewReply;

import java.util.Optional;
import java.util.UUID;

public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {


    Optional<ReviewReply> findByReviewReplyUuid(UUID replyUuid);

    @Query("SELECT r FROM ReviewReply r WHERE r.deletedAt IS NULL AND r.reviewId = :reviewId")
    Page<ReviewReply> findAllByDeletedAtIsNull(@Param("reviewId")Long reviewId, Pageable pageable);

    Optional<ReviewReply> findByReviewIdAndReviewReplyUuidAndDeletedAtIsNull(Long reviewId, UUID reviewReplyUuid);
}
