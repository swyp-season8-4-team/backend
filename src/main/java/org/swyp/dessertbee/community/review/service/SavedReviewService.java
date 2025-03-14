package org.swyp.dessertbee.community.review.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.community.mate.entity.SavedMate;
import org.swyp.dessertbee.community.review.dto.response.ReviewPageResponse;
import org.swyp.dessertbee.community.review.dto.response.ReviewResponse;
import org.swyp.dessertbee.community.review.entity.Review;
import org.swyp.dessertbee.community.review.entity.ReviewStatistics;
import org.swyp.dessertbee.community.review.entity.SavedReview;
import org.swyp.dessertbee.community.review.repository.ReviewRepository;
import org.swyp.dessertbee.community.review.repository.ReviewStatisticsRepository;
import org.swyp.dessertbee.community.review.repository.SavedReviewRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.service.UserService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedReviewService {

    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final SavedReviewRepository savedReviewRepository;
    private final ReviewStatisticsRepository reviewStatisticsRepository;
    private final ReviewService reviewService;

    /**
     * 커뮤니티 리뷰 저장
     * */
    @Transactional
    public void saveReview(UUID reviewUuid) {

        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userService.getCurrentUser();

        Review review = reviewRepository.findByReviewUuidAndDeletedAtIsNull(reviewUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMUNITY_REVIEW_NOT_FOUND));

        Long userId = user.getId();

        try{

            userService.findById(userId);

            SavedReview savedReview = savedReviewRepository.findByReview_ReviewIdAndUserId(review.getReviewId(), userId);

            if(savedReview != null) {
                throw new BusinessException(ErrorCode.DUPLICATION_SAVED_REVIEW);
            }

            savedReviewRepository.save(
                    SavedReview.builder()
                            .review(review)
                            .userId(user.getId())
                    .build()
            );

        }catch (BusinessException e) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

    }


    /**
     * 커뮤니티 리뷰 저장 삭제
     * */
    @Transactional
    public void deleteSavedReview(UUID reviewUuid) {

        UserEntity user = userService.getCurrentUser();

        Review review = reviewRepository.findByReviewUuidAndDeletedAtIsNull(reviewUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMUNITY_REVIEW_NOT_FOUND));

        Long userId = user.getId();

        try {

            userService.findById(userId);
            SavedReview savedReview = savedReviewRepository.findByReview_ReviewIdAndUserId(review.getReviewId(), userId);

            if(savedReview == null) {
                throw new BusinessException(ErrorCode.SAVED_REVIEW_NOT_FOUND);
            }


            savedReviewRepository.delete(savedReview);
        }catch (BusinessException e) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

    }

    /**
     * 커뮤니티 리뷰 저장 전체보기
     * */
    public ReviewPageResponse getSavedReviews(Pageable pageable) {

        // getCurrentUser() 내부에서 SecurityContext를 통해 현재 사용자 정보를 가져옴
        UserEntity user = userService.getCurrentUser();
        Long currentUserId = (user != null) ? user.getId() : null;

        Page<SavedReview> reviewsPage = savedReviewRepository.findByUserId(pageable, currentUserId);


        List<ReviewResponse> reviews = reviewsPage.stream()
                .map(review -> {
                    ReviewStatistics reviewStatistics = reviewStatisticsRepository.findByReviewId(review.getReview().getReviewId());
                    return reviewService.mapToReviewDetailResponse(review.getReview(), currentUserId, reviewStatistics.getViews());
                })
                .toList();

        return new ReviewPageResponse(reviews, reviewsPage.isLast());
    }
}
