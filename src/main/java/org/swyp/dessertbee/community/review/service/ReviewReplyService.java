package org.swyp.dessertbee.community.review.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.common.entity.ImageType;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.common.service.ImageService;
import org.swyp.dessertbee.community.review.dto.request.ReviewReplyCreateRequest;
import org.swyp.dessertbee.community.review.dto.response.ReviewReplyPageResponse;
import org.swyp.dessertbee.community.review.dto.response.ReviewReplyResponse;
import org.swyp.dessertbee.community.review.entity.Review;
import org.swyp.dessertbee.community.review.entity.ReviewReply;
import org.swyp.dessertbee.community.review.repository.ReviewReplyRepository;
import org.swyp.dessertbee.community.review.repository.ReviewRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;
import org.swyp.dessertbee.user.service.UserService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewReplyService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final ImageService imageService;
    private final UserService userService;


    public ReviewReplyResponse createReply(UUID reviewUuid, ReviewReplyCreateRequest request) {

        Long reviewId = validateReview(reviewUuid);

        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userService.getCurrentUser();

        ReviewReply reviewReply = reviewReplyRepository.save(
                ReviewReply.builder()
                        .reviewId(reviewId)
                        .userId(user.getId())
                        .content(request.getContent())
                .build()
        );

        return getReplyDetail(reviewUuid, reviewReply.getReviewReplyUuid());

    }

    /**
     * 커뮤니티 리뷰 댓글 하나만 조회
     * */
    public ReviewReplyResponse getReplyDetail(UUID reviewUuid, UUID reviewReplyUuid) {

        validateReview(reviewUuid);

        ReviewReply reviewReply = reviewReplyRepository.findByReviewReplyUuid(reviewReplyUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_REPLY_NOT_FOUND));
        UserEntity user = userRepository.findById(reviewReply.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        // 사용자별 프로필 이미지 조회


        String profileImage = imageService.getImageByTypeAndId(ImageType.PROFILE, user.getId());

        return ReviewReplyResponse.fromEntity(reviewReply, reviewUuid, user, profileImage);
    }

    /**
     * 커뮤니티 댓글 전체 조회
     * */
    @Transactional
    public ReviewReplyPageResponse getReplies(UUID reviewUuid, Pageable pageable) {

        Long reviewId = validateReview(reviewUuid);

        Page<ReviewReply> repliesPage = reviewReplyRepository.findAllByDeletedAtIsNull(reviewId, pageable);

        List<ReviewReplyResponse> reliesResponses = repliesPage.getContent()
                .stream()
                .map( reviewReply -> getReplyDetail(reviewUuid, reviewReply.getReviewReplyUuid()))
                .collect(Collectors.toList());

        boolean isLast = repliesPage.isLast();

        Long count = repliesPage.getTotalElements();

        return new ReviewReplyPageResponse(reliesResponses, isLast, count);
    }


    /**
     * 커뮤니티 리뷰 댓글 수정
     * */
    @Transactional
    public void updateReply(UUID reviewUuid, UUID reviewReplyUuid, ReviewReplyCreateRequest request) {

        Long reviewId = validateReview(reviewUuid);

        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userService.getCurrentUser();

        ReviewReply reviewReply = reviewReplyRepository.findByReviewIdAndReviewReplyUuidAndDeletedAtIsNull(reviewId, reviewReplyUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_REPLY_NOT_FOUND));

        if(!reviewReply.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.REPLY_NOT_AUTHOR);
        }

        reviewReply.update(request.getContent());
    }

    /**
     * 커뮤니티 리뷰 댓글 삭제
     * */
    @Transactional
    public void deleteReply(UUID reviewUuid, UUID reviewReplyUuid) {

        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userService.getCurrentUser();

        Long reviewId = validateReview(reviewUuid);


        ReviewReply reviewReply = reviewReplyRepository.findByReviewIdAndReviewReplyUuidAndDeletedAtIsNull(reviewId, reviewReplyUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_REPLY_NOT_FOUND));

        if(!reviewReply.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.REPLY_NOT_AUTHOR);
        }

        try {

            reviewReply.softDelete();

            reviewReplyRepository.save(reviewReply);
        }catch (Exception e) {

            System.out.println("❌ 커뮤니티 댓글 삭제 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("커뮤니티 댓글 삭제 실패: " + e.getMessage(), e);
        }

    }

    /**
     * Review만 유효성 검사
     * */
    public Long validateReview(UUID reviewUuid) {

        Review review = reviewRepository.findByReviewUuid(reviewUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_REPLY_NOT_FOUND));

        return review.getReviewId();
    }


}
